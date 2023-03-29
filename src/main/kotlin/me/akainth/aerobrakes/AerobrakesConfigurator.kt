package me.akainth.aerobrakes

import me.akainth.tape.dimensions.feet
import net.miginfocom.swing.MigLayout
import net.sf.openrocket.document.Simulation
import net.sf.openrocket.gui.SpinnerEditor
import net.sf.openrocket.gui.adaptors.DoubleModel
import net.sf.openrocket.gui.adaptors.IntegerModel
import net.sf.openrocket.gui.components.BasicSlider
import net.sf.openrocket.gui.components.UnitSelector
import net.sf.openrocket.plugin.Plugin
import net.sf.openrocket.simulation.extension.AbstractSwingSimulationExtensionConfigurator
import net.sf.openrocket.unit.UnitGroup
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.*
import javax.swing.border.TitledBorder


@Plugin
class AerobrakesConfigurator : AbstractSwingSimulationExtensionConfigurator<AerobrakesExtension>(
    AerobrakesExtension::class.java
) {
    override fun getConfigurationComponent(
        extension: AerobrakesExtension,
        simulation: Simulation,
        panel: JPanel
    ): JComponent {
        panel.layout = GridLayout(3, 1)

        JPanel(MigLayout()).also { fieldset ->
            fieldset.border = TitledBorder("Configuration")

            DoubleModel(extension, "TargetApogee", UnitGroup.UNITS_DISTANCE, 0.0, 17600.0).also { m ->
                fieldset.add(JLabel("Target Apogee"))

                val spinner = JSpinner(m.spinnerModel)
                val spinnerEditor = SpinnerEditor(spinner, 9)
                spinner.editor = spinnerEditor
                fieldset.add(spinner)

                val unitSelector = UnitSelector(m)
                fieldset.add(unitSelector, "wrap")
            }

            DoubleModel(extension, "DeploymentDelay", UnitGroup.UNITS_TIME_STEP, 3.0, 10.0).also { m ->
                fieldset.add(JLabel("Deployment Delay"))

                val spinner = JSpinner(m.spinnerModel)
                val spinnerEditor = SpinnerEditor(spinner, 9)
                spinner.editor = spinnerEditor
                fieldset.add(spinner)

                val unitSelector = UnitSelector(m)
                fieldset.add(unitSelector, "wrap")
            }

            panel.add(fieldset, "span")
        }

        JPanel(MigLayout()).also { fieldset ->
            fieldset.border = TitledBorder("Apogee Prediction")

            DoubleModel(extension, "FalloffWindow", UnitGroup.UNITS_TIME_STEP, 0.0, 60.0).also { m ->
                fieldset.add(JLabel("Use Last"))

                val spinner = JSpinner(m.spinnerModel)
                val spinnerEditor = SpinnerEditor(spinner, 9)
                spinner.editor = spinnerEditor
                fieldset.add(spinner)

                val unitSelector = UnitSelector(m)
                fieldset.add(unitSelector, "wrap")
            }

            panel.add(fieldset, "span")
        }

        JPanel(MigLayout()).also { fieldset ->
            fieldset.border = TitledBorder("PID Gains")

            DoubleModel(extension, "KP", UnitGroup.UNITS_COEFFICIENT, 0.0, 10.0).also { m ->
                fieldset.add(JLabel("kP"))

                val spinner = JSpinner(m.spinnerModel)
                val spinnerEditor = SpinnerEditor(spinner, 9)
                spinner.editor = spinnerEditor
                fieldset.add(spinner)
            }

            DoubleModel(extension, "KI", UnitGroup.UNITS_COEFFICIENT, 0.0, 10.0).also { m ->
                fieldset.add(JLabel("kI"))

                val spinner = JSpinner(m.spinnerModel)
                val spinnerEditor = SpinnerEditor(spinner, 9)
                spinner.editor = spinnerEditor
                fieldset.add(spinner)
            }

            DoubleModel(extension, "KD", UnitGroup.UNITS_COEFFICIENT, 0.0, 10.0).also { m ->
                fieldset.add(JLabel("kD"))

                val spinner = JSpinner(m.spinnerModel)
                val spinnerEditor = SpinnerEditor(spinner, 9)
                spinner.editor = spinnerEditor
                fieldset.add(spinner)
            }

            panel.add(fieldset, "span")
        }
        return panel
    }
}
