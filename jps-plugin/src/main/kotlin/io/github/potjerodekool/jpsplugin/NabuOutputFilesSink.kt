package io.github.potjerodekool.jpsplugin

import org.jetbrains.jps.builders.java.dependencyView.Callbacks
import org.jetbrains.jps.incremental.CompileContext
import org.jetbrains.jps.incremental.ModuleLevelBuilder
import java.io.File

class NabuOutputFilesSink(
    context: CompileContext,
    outputConsumer: ModuleLevelBuilder.OutputConsumer,
    callback: Callbacks.Backend,
    chunkName: String
) {
    private val compiledFiles = mutableSetOf<File>()

    fun addCompiledFile(file: File) {
        compiledFiles.add(file)
    }

    fun getSuccessfullyCompiled(): Set<File> {
        return compiledFiles.toSet()
    }
}