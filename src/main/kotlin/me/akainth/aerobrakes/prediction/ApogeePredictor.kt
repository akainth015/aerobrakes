package me.akainth.aerobrakes.prediction

import me.akainth.tape.dimensions.Altitude
import net.sf.openrocket.simulation.SimulationStatus

interface ApogeePredictor {
    fun makeApogeePrediction(simulation: SimulationStatus): Altitude
}
