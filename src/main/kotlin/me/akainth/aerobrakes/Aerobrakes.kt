@file:JvmName("Aerobrakes")

package me.akainth.aerobrakes

import me.akainth.tape.dimensions.*
import net.sf.openrocket.aerodynamics.AerodynamicForces
import net.sf.openrocket.aerodynamics.BarrowmanCalculator
import net.sf.openrocket.aerodynamics.FlightConditions
import net.sf.openrocket.simulation.FlightDataType
import net.sf.openrocket.simulation.SimulationStatus
import net.sf.openrocket.simulation.exception.SimulationException
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener
import java.io.File
import java.lang.Math.random
import kotlin.math.pow

@Suppress("unused")
class Aerobrakes : AbstractSimulationListener() {
    private var a = -49.4174651698608
    private var b = 331.379008606358
    private var c = -419.277927705465

    private var errorIntegral = 0.0
    private var lastError: Double = 0.0
    private var lastTime: Double = 0.0

    private var motorThrottle = 0.0
    private var motorPosition = 0.0

    private fun evaluatePolynomial(x: Double): Double {
        return a * x.pow(1.5) + b * x + c
    }

    private val logFile = File("${System.getProperty("user.home")}/Documents/noFinsADAS1:.csv").let {
        it.createNewFile()
        it.printWriter()
    }

    override fun startSimulation(status: SimulationStatus) {
//        logFile.println("Time, Altitude, , ")
    }

    override fun endSimulation(status: SimulationStatus, exception: SimulationException?) {
        logFile.close()
    }

    private var logFrame = true

    override fun postAerodynamicCalculation(status: SimulationStatus, forces: AerodynamicForces): AerodynamicForces {
        if (status.simulationTime < (DEPLOYMENT_DELAY - SCAN_WIDTH).seconds || status.simulationTime == lastTime) {
            lastTime = status.simulationTime
            return forces
        }
        // Filter data to only include data from after deployment that is from within SCAN_WIDTH seconds
        val (times, altitudes) = status.flightData.get(FlightDataType.TYPE_TIME)
            .map { it.seconds }
            .zip(status.flightData.get(FlightDataType.TYPE_ALTITUDE)?.map {
                it.meters + (5 * random() - 2.5).meters
            } ?: return forces)
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

        val p = kP * error
        val i = kI * errorIntegral

        val dT = status.simulationTime - lastTime
        val hasDT = lastTime != 0.0 && dT != 0.0

        val derivativeTerm = if (hasDT) {
            if (lastError == 0.0) {
                lastError = error
            }
            val dE = error - lastError
            dE / dT
        } else 0.0

        errorIntegral += if (hasDT) {
            error * dT
        } else 0.0

        val d = kD * derivativeTerm

        motorPosition += motorThrottle * dT
        if (motorPosition < 0 || motorPosition > SAFEST_POSITION) {
            System.err.println("Motor position ($motorPosition) is outside the safe range.")
            motorPosition.coerceIn((0.0)..SAFEST_POSITION)
        }

        motorThrottle = -(p + i + d)
        motorThrottle = motorThrottle.coerceIn((-1.0)..1.0)

        // Safety code to prevent over-retracting or over-extending the fins
        val futurePosition = motorPosition + motorThrottle * dT * 1.15
        if (futurePosition < 0 || futurePosition > SAFEST_POSITION * 0.95 || status.simulationTime < DEPLOYMENT_DELAY.seconds) {
            motorThrottle = 0.0
        }

        lastError = error
        lastTime = status.simulationTime

        if (logFrame) {
            logFile.println("${status.simulationTime}, ${altitudes.last().meters}, 0, 0")
        }
        logFrame = !logFrame

        val conditions = FlightConditions(status.configuration)

        val stagnationCd = BarrowmanCalculator.calculateStagnationCD(conditions.mach)

        val refArea = motorPosition / SAFEST_POSITION * FIN_AREA.metersMeters * 2 // because there are 2 fins

        val cd = 1.28 // sourced from Professor Pascale

        forces.caxial += cd * stagnationCd * refArea / conditions.refArea

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
        val SCAN_WIDTH = 1.seconds

        /**
         * The target apogee of the rocket's deployment
         */
        val TARGET_APOGEE = 5280.feet

        /**
         * The number of seconds the calibration code ran for before it was stopped
         */
        private val CALIBRATION_TIME = 5.8.seconds

        /**
         * The duty cycle at which the calibration code ran
         */
        private const val CALIBRATION_THROTTLE = 0.1

        /**
         * The maximum extension position of the motor, derived from the calibration constants
         */
        val SAFEST_POSITION = CALIBRATION_TIME.seconds * CALIBRATION_THROTTLE

        /**
         * The wetted surface area of the fins
         */
        val FIN_AREA = 6.94.inchesInches

        // PID gains
        const val kP = 2
        const val kI = 0
        const val kD = 6

        /**
         * Learning rate for gradient descent polynomial regression
         */
        const val ALPHA = 0.05
    }
}
