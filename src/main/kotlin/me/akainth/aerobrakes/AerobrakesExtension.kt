package me.akainth.aerobrakes

import me.akainth.tape.dimensions.feet
import me.akainth.tape.dimensions.meters
import me.akainth.tape.dimensions.metersMeters
import me.akainth.tape.dimensions.seconds
import net.sf.openrocket.gui.plot.PlotConfiguration
import net.sf.openrocket.simulation.FlightDataType
import net.sf.openrocket.simulation.SimulationConditions
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension
import net.sf.openrocket.unit.UnitGroup


val predictedApogeeDataType: FlightDataType =
    FlightDataType.getType("Predicted Apogee", "PredApg", UnitGroup.UNITS_DISTANCE)

class AerobrakesExtension : AbstractSimulationExtension("UCSC ADAS") {
    override fun initialize(simulationConditions: SimulationConditions) {
        // This is a hack - it injects the predicted apogee into the "Vertical motion vs. time" plot preset
        // It is necessary to do it like this because DEFAULT_CONFIGURATIONS is a final array
        // Luckily, that doesn't mean its entries are too!
        // https://github.com/openrocket/openrocket/blob/422f0049e2dae17e4ae0c5728c7882980d8d7140/swing/src/net/sf/openrocket/gui/plot/PlotConfiguration.java#L30
        if (PlotConfiguration.DEFAULT_CONFIGURATIONS[0].typeCount != 4) {
            PlotConfiguration.DEFAULT_CONFIGURATIONS[0].addPlotDataType(predictedApogeeDataType, 0)
        }

        simulationConditions.simulationListenerList += AerobrakesSimulationListener(
            getKP(),
            getKI(),
            getKD(),
            getFalloffWindow().seconds,
            getTargetApogee().meters,
            getDeploymentDelay().seconds,
            getSurfaceArea().metersMeters
        )
    }

    override fun getFlightDataTypes(): MutableList<FlightDataType> {
        return mutableListOf(predictedApogeeDataType)
    }

    @Suppress("SpellCheckingInspection")
    override fun getDescription(): String {
        return "The ADaptive Aerobraking System (ADAS) predicts " +
                "the apogee of the rocket, and in response to whether that " +
                "prediction is higher or lower than the target apogee, " +
                "opens or closes a pair of aerobrakes"
    }

    fun getLearningRate(): Double {
        return config.getDouble("learningRate", 0.01)
    }

    fun setLearningRate(learningRate: Double) {
        config.put("learningRate", learningRate)
        fireChangeEvent()
    }

    fun getStepsPerPoint(): Int {
        return config.getInt("stepsPerPoint", 5)
    }

    fun setStepsPerPoint(stepsPerPoint: Int): Unit {
        config.put("stepsPerPoint", stepsPerPoint)
        fireChangeEvent()
    }

    fun getFalloffWindow(): Double {
        return config.getDouble("falloffWindow", 0.5)
    }

    fun setFalloffWindow(falloffWindow: Double) {
        config.put("falloffWindow", falloffWindow)
    }

    fun getKP(): Double {
        return config.getDouble("kP", 1.0)
    }

    fun setKP(value: Double) {
        config.put("kP", value)
        fireChangeEvent()
    }

    fun getKI(): Double {
        return config.getDouble("kI", 1.0)
    }

    fun setKI(value: Double) {
        config.put("kI", value)
        fireChangeEvent()
    }

    fun getKD(): Double {
        return config.getDouble("kD", 1.0)
    }

    fun setKD(value: Double) {
        config.put("kD", value)
        fireChangeEvent()
    }

    fun getTargetApogee(): Double {
        return config.getDouble("targetApogee", 4600.feet.meters)
    }

    fun setTargetApogee(value: Double) {
        config.put("targetApogee", value)
        fireChangeEvent()
    }

    fun getDeploymentDelay(): Double {
        return config.getDouble("deploymentDelay", 5.0)
    }

    fun setDeploymentDelay(value: Double) {
        config.put("deploymentDelay", value)
        fireChangeEvent()
    }

    fun getSurfaceArea(): Double {
        return config.getDouble("surfaceArea", 3.51)
    }

    fun setSurfaceArea(value: Double) {
        config.put("surfaceArea", value)
        fireChangeEvent()
    }
}
