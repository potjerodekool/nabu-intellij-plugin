package io.github.potjerodekool.jpsplugin

import io.github.potjerodekool.nabu.compiler.client.ByteCodeEvent
import io.github.potjerodekool.nabu.compiler.client.DiagnosticEvent
import io.github.potjerodekool.nabu.compiler.client.LightweightClient

import org.jetbrains.jps.incremental.CompileContext
import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.jps.incremental.ModuleLevelBuilder
import org.jetbrains.jps.incremental.messages.BuildMessage
import org.jetbrains.jps.incremental.messages.CompilerMessage
import java.io.File
import java.io.IOException

object NabuBatchCompiler {
    @Throws(IOException::class)
    fun compile(
        files: List<File>,
        options: Map<String, String>,
        context: CompileContext,
        outputSink: NabuOutputFilesSink,
        outputConsumer: ModuleLevelBuilder.OutputConsumer,
        target: ModuleBuildTarget
    ) {
        log(context, "Starting compiling nabu 1")

        // --class-path

        startCompilerProcess(context, {
            val client = LightweightClient()
            client.compile(
                options,
                { event ->
                    when (event) {
                        is DiagnosticEvent -> {
                            process(context, event, outputSink, outputConsumer, target)
                        }
                        is ByteCodeEvent -> {
                            process(event, outputConsumer, target)
                        }
                    }
                }
            )
        })
    }

    private fun startCompilerProcess(
        context: CompileContext,
        action: () -> Unit
    ) {

        action()
    }

    fun log(context: CompileContext, message: String) {
        context.processMessage(
            CompilerMessage(
                "Nabu",
                BuildMessage.Kind.INFO,
                message
            )
        )
    }

    fun process(
        context: CompileContext,
        event: DiagnosticEvent,
        outputSink: NabuOutputFilesSink,
        outputConsumer: ModuleLevelBuilder.OutputConsumer,
        target: ModuleBuildTarget
    ) {
        val kind = when (event.kind()) {
            DiagnosticEvent.Kind.ERROR -> BuildMessage.Kind.ERROR
            DiagnosticEvent.Kind.WARN, DiagnosticEvent.Kind.MANDATORY_WARNING -> BuildMessage.Kind.WARNING
            DiagnosticEvent.Kind.NOTE -> BuildMessage.Kind.INFO
            DiagnosticEvent.Kind.OTHER -> BuildMessage.Kind.OTHER
        }

        context.processMessage(
            CompilerMessage(
                "Nabu",
                kind,
                event.message
            )
        )
    }

    fun process(
        event: ByteCodeEvent,
        outputConsumer: ModuleLevelBuilder.OutputConsumer,
        target: ModuleBuildTarget
    ) {
        val sourceFileName = event.sourceFileName
        val classFileName = event.classFileName

        outputConsumer.registerOutputFile(
            target,
            File(classFileName),
            listOf(sourceFileName)
        )
    }
}