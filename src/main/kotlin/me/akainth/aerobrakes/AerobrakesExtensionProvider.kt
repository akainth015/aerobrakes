package me.akainth.aerobrakes

import net.sf.openrocket.plugin.Plugin
import net.sf.openrocket.simulation.extension.AbstractSimulationExtensionProvider

@Plugin
class AerobrakesExtensionProvider : AbstractSimulationExtensionProvider(
    AerobrakesExtension::class.java,
    "UCSC ADAS"
)
