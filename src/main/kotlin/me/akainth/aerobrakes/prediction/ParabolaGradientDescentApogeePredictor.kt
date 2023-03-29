package me.akainth.aerobrakes.prediction
//
//import me.akainth.tape.dimensions.*
//import kotlin.math.pow
//
//class ParabolaGradientDescentApogeePredictor(
//    private val falloffWindow: Time,
//    private val learningRate: Double,
//    private val stepsPerPoint: Int,
//) : ApogeePredictor {
//    private var a = -10.8159
//    private var b = 149.918
//    private var c = -107.667
//
//    private var data: MutableList<Pair<Time, Altitude>> = mutableListOf()
//
//    /**
//     * @return the y-value of the parabola at the given x value
//     */
//    private fun getAltitudePrediction(time: Time): Altitude {
//        return (a * time.seconds.pow(1.8) + b * time.seconds + c).meters
//    }
//
//    /**
//     * @return the y-value of the maxima (or minima) of the parabola.
//     * Typically, it will be the maxima, but if prediction is going poorly,
//     * it is possible that it will be the minima instead.
//     */
//    override fun getApogeePrediction(): Altitude {
//        // This is the x-coordinate of a parabola's critical point
//        val apogeeTime = (-b / (1.8 * a)).pow(1 / (1.8 - 1))
//
//        return getAltitudePrediction(apogeeTime.seconds)
//    }
//
//    /**
//     * Perform a gradient update step using the given time and altitude pair.
//     * This will result in a more accurate apogee prediction
//     */
//    override fun onFlightMeasurement(time: Time, altitude: Altitude) {
//        data += Pair(time, altitude)
//
//        // Remove data points that may not reflect the latest trajectory of the rocket.
//        // Its trajectory will likely change due to deployment of aerobrakes or other
//        // geometric changes during flight
//        data = data.dropWhile { (t, _) -> t.seconds < (time - falloffWindow).seconds }.toMutableList()
//
//        for (i in 0 until stepsPerPoint) {
//            val errors = data.map { (t, y) ->
//                val error = y - getAltitudePrediction(t)
//                // Division by 2e3 can be considered feature normalization and prevents arithmetic
//                // overflows while performing the average operation
//                val smallError = error.meters / 2e3
//                smallError.meters
//            }
//
//            // The function F optimized by gradient descent is 1/2*(altitude - prediction)^2
//            val derivativeA = errors.map { error -> error.meters * -time.seconds.pow(1.8) }.average()
//            val derivativeB = errors.map { error -> error.meters * -time.seconds }.average()
//            val derivativeC = errors.map { error -> error.meters * -1 }.average()
//
//            // These numbers are further reduced so that c can be adjusted faster than b,
//            // which in turn is adjusted faster than 'a'. This is some form of feature normalization,
//            // but I do not wish to actually modify the raw time and altitude inputs because
//            // their scale is useful for debugging
//            a -= learningRate * derivativeA / 219.712
//            b -= learningRate * derivativeB / 20
//            c -= learningRate * derivativeC
//
//        }
//    }
//}
