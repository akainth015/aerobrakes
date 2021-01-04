@file:JvmName("Aerobrakes")

package me.akainth.aerobrakes

import me.akainth.tape.dimensions.feet
import net.sf.openrocket.aerodynamics.AerodynamicForces
import net.sf.openrocket.simulation.SimulationStatus
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener

class Aerobrakes : AbstractSimulationListener() {
    override fun postAerodynamicCalculation(status: SimulationStatus, forces: AerodynamicForces): AerodynamicForces {
        println(TARGET_APOGEE)
        return forces
    }

    companion object {
        val TARGET_APOGEE = 5000.feet
    }
}
