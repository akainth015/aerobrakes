@file:JvmName("Aerobrakes")

package me.akainth.aerobrakes

import me.akainth.aerobrakes.prediction.ParabolaLeastSquaresApogeePredictor
import me.akainth.aerobrakes.prediction.WpiApogeePredictor
import me.akainth.tape.dimensions.*
import net.sf.openrocket.aerodynamics.AerodynamicForces
import net.sf.openrocket.simulation.FlightDataType
import net.sf.openrocket.simulation.SimulationStatus
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener

class AerobrakesSimulationListener(
    private val kP: Double,
    private val kI: Double,
    private val kD: Double,
    falloffWindow: Time,
    private val targetApogee: Altitude,
    private val deploymentDelay: Time,
    private val surfaceArea: Area
) : AbstractSimulationListener() {
    private val predictor = WpiApogeePredictor()


    /**
     * Predict the apogee of the flight, then simulate the actuation of the fins
     */
    override fun postAerodynamicCalculation(
        simulation: SimulationStatus,
        forces: AerodynamicForces
    ): AerodynamicForces {
        if (simulation.simulationTime < deploymentDelay.seconds) {
            return forces
        }

        val predictedApogee = predictor.makeApogeePrediction(simulation)
        simulation.flightData.setValue(predictedApogeeDataType, predictedApogee.meters)

        return forces
    }
}
