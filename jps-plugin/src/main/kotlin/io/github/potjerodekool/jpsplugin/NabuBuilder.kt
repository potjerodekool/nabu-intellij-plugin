package io.github.potjerodekool.jpsplugin

import com.intellij.openapi.util.text.Strings
import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.builders.DirtyFilesHolder
import org.jetbrains.jps.builders.java.JavaBuilderUtil
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor
import org.jetbrains.jps.incremental.BuilderCategory
import org.jetbrains.jps.incremental.CompileContext
import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.jps.incremental.ModuleLevelBuilder
import org.jetbrains.jps.incremental.messages.BuildMessage
import org.jetbrains.jps.incremental.messages.CompilerMessage
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.java.JpsJavaClasspathKind
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter


class NabuBuilder : ModuleLevelBuilder(BuilderCategory.SOURCE_PROCESSOR) {
    override fun getPresentableName(): String {
        return Strings.capitalize("nabu")
    }

    override fun build(
        context: CompileContext,
        chunk: ModuleChunk,
        dirtyFilesHolder: DirtyFilesHolder<JavaSourceRootDescriptor?, ModuleBuildTarget?>,
        outputConsumer: OutputConsumer
    ): ExitCode {
        val nabuFiles: MutableList<File> = ArrayList()


        dirtyFilesHolder.processDirtyFiles { _: ModuleBuildTarget?, file: File?, _: JavaSourceRootDescriptor? ->
            if (file!!.getName().endsWith(".nabu")) {
                nabuFiles.add(file)
            }
            true
        }

        if (nabuFiles.isEmpty()) {
            return ExitCode.NOTHING_DONE
        }

        val options = mutableMapOf<String, String>()
        configure(chunk, options)
        return compile(
            context,
            chunk,
            nabuFiles,
            options,
            outputConsumer
        )
    }

    private fun compile(
        context: CompileContext,
        chunk: ModuleChunk,
        nabuFiles: List<File>,
        options: Map<String, String>,
        outputConsumer: OutputConsumer
    ): ExitCode {
        val outputSink = NabuOutputFilesSink(
            context,
            outputConsumer,
            JavaBuilderUtil.getDependenciesRegistrar(context),
            chunk.presentableShortName
        )

        val filesWithErrors: MutableCollection<File>? = null

        try {
            chunk.targets.forEach { target ->
                NabuBatchCompiler.compile(nabuFiles, options, context, outputSink, outputConsumer, target)
            }
        } catch (e: Exception) {
            val stringWriter = StringWriter()
            val writer = PrintWriter(stringWriter)
            e.printStackTrace(writer)
            writer.flush()

            context.processMessage(
                CompilerMessage(
                    "Nabu",
                    BuildMessage.Kind.ERROR,
                    "Nabu compiler failed from builder: " + e.message + "\n" + stringWriter.toString()
                )
            )

            return ExitCode.ABORT
        } finally {
            JavaBuilderUtil.registerFilesToCompile(context, nabuFiles)
            if (filesWithErrors != null) {
                JavaBuilderUtil.registerFilesWithErrors(context, filesWithErrors)
            }

            NabuBuilderUtil.registerSuccessfullyCompiled(context, outputSink.getSuccessfullyCompiled())
        }

        return ExitCode.OK
    }

    private fun configure(
        chunk: ModuleChunk,
        options: MutableMap<String, String>
    ) {
        chunk.targets.forEach { target ->
            val module = target.module
            val isTests = target.isTests

            val sourcePaths = mutableListOf<File>()

            module.sourceRoots.forEach { sourceRoot ->
                if (isTests && sourceRoot.rootType == JavaSourceRootType.TEST_SOURCE) {
                    sourcePaths.add(sourceRoot.file);
                } else if (!isTests && sourceRoot.rootType == JavaSourceRootType.SOURCE) {
                    sourcePaths.add(sourceRoot.file);
                }
            }

            // === CLASSPATH ===
            val classpath = JpsJavaExtensionService.dependencies(module)
                .recursively()
                .exportedOnly()  // optioneel
                .includedIn(JpsJavaClasspathKind.compile(isTests))
                .classes()
                .getRoots();

            // === OUTPUT DIRECTORY ===
            val outputDir = JpsJavaExtensionService.getInstance()
                .getOutputDirectory(module, isTests) as File

            options["--source-path"] = sourcePaths.joinToString(File.pathSeparator)
            options["--class-path"] = classpath.joinToString(File.pathSeparator)
            options["-d"] = outputDir.absolutePath
        }


    }

    override fun getCompilableFileExtensions(): List<String> {
        return listOf("nabu")
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
}