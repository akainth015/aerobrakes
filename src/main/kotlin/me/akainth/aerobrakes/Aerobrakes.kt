@file:JvmName("Aerobrakes")

package me.akainth.aerobrakes

import me.akainth.tape.dimensions.div
import me.akainth.tape.dimensions.meters
import me.akainth.tape.dimensions.minus
import me.akainth.tape.dimensions.seconds
import net.sf.openrocket.aerodynamics.AerodynamicForces
import net.sf.openrocket.simulation.FlightDataType
import net.sf.openrocket.simulation.SimulationStatus
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener
import kotlin.math.pow

class Aerobrakes : AbstractSimulationListener() {
    private var a = -51.0
    private var b = 330.0
    private var c = -420.0

    private var motorPosition = 0

    private fun evaluatePolynomial(x: Double): Double {
        return a * x.pow(1.5) + b * x + c
    }

    override fun postAerodynamicCalculation(status: SimulationStatus, forces: AerodynamicForces): AerodynamicForces {
        if (status.simulationTime < DEPLOYMENT_DELAY.seconds) {
            return forces
        }
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

        val predictedApogee = evaluatePolynomial((-b / 1.5 / a).pow(2)).meters
        println("predictedApogee = ${predictedApogee}")

        return forces
    }

    companion object {
        val DEPLOYMENT_DELAY = 5.seconds
        val SCAN_WIDTH = 2.seconds
        val TARGET_APOGEE = 5000.meters


        const val kP = 1
        const val kI = 0
        const val kD = 0
        const val ALPHA = 0.05
    }
}
