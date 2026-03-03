package io.github.potjerodekool.jpsplugin

import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.builders.DirtyFilesHolder
import org.jetbrains.jps.builders.FileProcessor
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor
import org.jetbrains.jps.incremental.BuilderCategory
import org.jetbrains.jps.incremental.CompileContext
import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.jps.incremental.ModuleLevelBuilder
import org.jetbrains.jps.incremental.messages.BuildMessage
import org.jetbrains.jps.incremental.messages.CompilerMessage
import java.io.File


class NabuBuilder : ModuleLevelBuilder(BuilderCategory.SOURCE_PROCESSOR) {
    override fun getPresentableName(): String {
        return "Nabu Builder"
    }

    override fun build(
        context: CompileContext,
        chunk: ModuleChunk,
        dirtyFilesHolder: DirtyFilesHolder<JavaSourceRootDescriptor?, ModuleBuildTarget?>,
        outputConsumer: OutputConsumer
    ): ExitCode {
        val nabuFiles: MutableList<File?> = ArrayList<File?>()

        dirtyFilesHolder.processDirtyFiles(FileProcessor { target: ModuleBuildTarget?, file: File?, root: JavaSourceRootDescriptor? ->
            if (file!!.getName().endsWith(".nabu")) {
                nabuFiles.add(file)
            }
            true
        })

        if (nabuFiles.isEmpty()) {
            return ExitCode.NOTHING_DONE
        }

        context.processMessage(
            CompilerMessage(
                "Nabu",
                BuildMessage.Kind.INFO,
                "Compiling " + nabuFiles.size + " Nabu files"
            )
        )

        try {
            NabuBatchCompiler.compile(nabuFiles)
        } catch (e: Exception) {
            context.processMessage(
                CompilerMessage(
                    "Nabu",
                    BuildMessage.Kind.ERROR,
                    "Nabu compiler failed: " + e.message
                )
            )

            return ExitCode.ABORT
        }

        return ExitCode.OK
    }

    override fun getCompilableFileExtensions(): List<String> {
        return listOf("nabu")
    }
}