package io.github.potjerodekool.jpsplugin

import org.jetbrains.jps.incremental.BuilderService
import org.jetbrains.jps.incremental.ModuleLevelBuilder

class NabuBuilderService : BuilderService() {

    override fun createModuleLevelBuilders(): MutableList<out ModuleLevelBuilder?> {
        return mutableListOf<ModuleLevelBuilder?>(NabuBuilder())
    }
}