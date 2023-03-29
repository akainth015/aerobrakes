package me.akainth.aerobrakes.prediction

import me.akainth.tape.dimensions.Altitude
import me.akainth.tape.dimensions.Time
import me.akainth.tape.dimensions.meters
import me.akainth.tape.dimensions.seconds
import net.sf.openrocket.simulation.FlightDataType
import net.sf.openrocket.simulation.SimulationStatus
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.linalg.inv
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import kotlin.math.pow

const val EXPONENT = 1.7

class ParabolaLeastSquaresApogeePredictor(
    private val falloffWindow: Time
) : ApogeePredictor {

    override fun makeApogeePrediction(simulation: SimulationStatus): Altitude {
        val (times, altitudes) = simulation.flightData.get(FlightDataType.TYPE_TIME).map(Number::seconds)
            .zip(
                simulation.flightData.get(FlightDataType.TYPE_ALTITUDE).map(Number::meters)
            )
            .takeLastWhile { (t, _) -> t.seconds > simulation.simulationTime - falloffWindow.seconds }
            .unzip()

        val X =
            times
                .map { time ->
                    mk[
                        1.0,
                        time.seconds,
                        time.seconds.pow(EXPONENT)
                    ]
                }
                .let(mk::ndarray)
        val y = mk.ndarray(altitudes.map(Altitude::meters))

        val coefficients = mk.linalg.inv(X.transpose() dot X) dot X.transpose() dot y

        val a = coefficients[2]
        val b = coefficients[1]

        val apogeeTime = (-b / (EXPONENT * a)).pow(1 / (EXPONENT - 1))

        val apogeeInput = mk.ndarray(mk[1.0, apogeeTime, apogeeTime.pow(EXPONENT)])

        return (coefficients dot apogeeInput).meters
    }
}
