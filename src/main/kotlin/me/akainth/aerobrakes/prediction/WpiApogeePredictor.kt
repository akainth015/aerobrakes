package me.akainth.aerobrakes.prediction

import me.akainth.tape.dimensions.Altitude
import me.akainth.tape.dimensions.meters
import net.sf.openrocket.aerodynamics.FlightConditions
import net.sf.openrocket.aerodynamics.WarningSet
import net.sf.openrocket.simulation.FlightDataType
import net.sf.openrocket.simulation.SimulationStatus
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * This apogee prediction model is based on the WPI HPRC's [publicly available source code](https://github.com/WPI-HPRC/ORBrake/blob/2eb956324d7c49ee82118b68ff5f99dcddb16a80/src/net/sf/openrocket/ORBrake/ORBrakeSimulationListener.java).
 */
class WpiApogeePredictor : ApogeePredictor {
    override fun makeApogeePrediction(simulation: SimulationStatus): Altitude {
        val alt = simulation.rocketPosition.z
        val velocity = simulation.rocketVelocity.length()
        val vertVelocity = simulation.rocketVelocity.z

        val mass = simulation.flightData.getLast(FlightDataType.TYPE_MASS)
        val Cd = simulation.simulationConditions.aerodynamicCalculator.getAerodynamicForces(
            simulation.configuration,
            FlightConditions(simulation.configuration),
            WarningSet()
        ).cd

        val gravity = simulation.simulationConditions.gravityModel.getGravity(simulation.rocketWorldPosition)
        val refArea = simulation.configuration.referenceArea

        val termVelocity = sqrt(2 * mass * gravity / (Cd * refArea * 1.225))
        val predApogee = alt + termVelocity.pow(2.0) / (2 * gravity) * ln(
            (vertVelocity.pow(2.0) + termVelocity.pow(2.0)) / termVelocity.pow(2.0)
        )
        return predApogee.meters
    }
}
