@file:JvmName("Aerobrakes")

package me.akainth.aerobrakes

import me.akainth.tape.dimensions.*
import net.sf.openrocket.aerodynamics.AerodynamicForces
import net.sf.openrocket.aerodynamics.BarrowmanCalculator
import net.sf.openrocket.aerodynamics.FlightConditions
import net.sf.openrocket.simulation.FlightDataType
import net.sf.openrocket.simulation.SimulationStatus
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener
import kotlin.math.pow

class Aerobrakes : AbstractSimulationListener() {
    private var a = -51.0
    private var b = 330.0
    private var c = -420.0

    private var errorIntegral = 0.0
    private var lastError: Double = 0.0
    private var lastTime: Double = 0.0

    private var motorThrottle = 0.0
    private var motorPosition = 0.0

    private fun evaluatePolynomial(x: Double): Double {
        return a * x.pow(1.5) + b * x + c
    }

    override fun postAerodynamicCalculation(status: SimulationStatus, forces: AerodynamicForces): AerodynamicForces {
        if (status.simulationTime < DEPLOYMENT_DELAY.seconds) {
            return forces
        }
        // Filter data to only include data from after deployment that is from within SCAN_WIDTH seconds
        val (times, altitudes) = status.flightData.get(FlightDataType.TYPE_TIME)
            .map { it.seconds }
            .zip(status.flightData.get(FlightDataType.TYPE_ALTITUDE)?.map { it.meters } ?: return forces)
            .takeLastWhile { (T, _) ->
                T.seconds >= status.simulationTime - SCAN_WIDTH.seconds
            }
            .unzip()

        val n = times.size

        if (n < 3) {
            return forces
        }

        // Min-max normalization for the data points
        val normTimes = times.map { it / 30.seconds }
        val normAltitudes = altitudes.map { it / 2000.meters }

        // Gradient descent
        val derivativeOfMSE = times.zip(normAltitudes)
            .map { (T, nA) ->
                val prediction = evaluatePolynomial(T.seconds).meters
                val normPrediction = prediction / 2000.meters

                nA - normPrediction
            }
            .sum() / n

        a -= ALPHA * derivativeOfMSE * normTimes.map { T -> -T.pow(1.5) }.sum()
        b -= ALPHA * derivativeOfMSE * normTimes.map { T -> -T }.sum()
        c += ALPHA * derivativeOfMSE

        val apogeeTime = (-b / 1.5 / a).pow(2)
        val predictedApogee = evaluatePolynomial(apogeeTime).meters

        // PID algorithm
        val error = (TARGET_APOGEE - predictedApogee).meters

        val P = kP * error
        val I = kI * errorIntegral

        val dT = status.simulationTime - lastTime
        val hasDT = lastTime != 0.0 && dT != 0.0

        val derivativeTerm = if (hasDT) {
            val dE = error - lastError
            dE / dT
        } else 0.0

        errorIntegral += if (hasDT) {
            error * dT
        } else 0.0

        val D = kD * derivativeTerm

        motorThrottle = (-(P + I + D)).coerceIn((-1.0)..1.0)

        // Safety code to prevent over-retracting or over-extending the fins
        val futurePosition = motorPosition + motorThrottle * dT
        if (futurePosition < 0 || futurePosition > SAFEST_POSITION) {
            motorThrottle = 0.0
        }

        motorPosition += motorThrottle * dT
        if (motorPosition < 0 || motorPosition > SAFEST_POSITION) {
            println("Motor position ($motorPosition) is outside the safe range")
            motorPosition.coerceIn((0.0)..SAFEST_POSITION)
        }

        lastError = error
        lastTime = status.simulationTime

        val conditions = FlightConditions(status.configuration)

        val stagnationCd = BarrowmanCalculator.calculateStagnationCD(conditions.mach)

        val refArea = motorPosition / SAFEST_POSITION * FIN_AREA.metersMeters

        val cd = refArea / conditions.refArea

        forces.caxial += cd * stagnationCd * refArea

        return forces
    }

    companion object {
        /**
         * The time to wait before simulating fin deployment
         */
        val DEPLOYMENT_DELAY = 5.seconds

        /**
         * Regression will include the last SCAN_WIDTH seconds of data
         */
        val SCAN_WIDTH = 2.seconds

        /**
         * The target apogee of the rocket's deployment
         */
        val TARGET_APOGEE = 5000.feet

        /**
         * The number of seconds the calibration code ran for before it was stopped
         */
        val CALIBRATION_TIME = 10.seconds

        /**
         * The duty cycle at which the calibration code ran
         */
        val CALIBRATION_THROTTLE = 0.1

        /**
         * The maximum extension position of the motor, derived from the calibration constants
         */
        val SAFEST_POSITION = CALIBRATION_TIME.seconds * CALIBRATION_THROTTLE

        /**
         * The wetted surface area of the fins
         */
        val FIN_AREA = 0.018.metersMeters

        // PID gains
        const val kP = 1
        const val kI = 0
        const val kD = 0

        /**
         * Learning rate for gradient descent polynomial regression
         */
        const val ALPHA = 0.05
    }
}
