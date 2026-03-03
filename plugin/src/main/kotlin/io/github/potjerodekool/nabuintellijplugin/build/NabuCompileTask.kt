package io.github.potjerodekool.nabuintellijplugin.build

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileTask
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.openapi.vfs.VirtualFile

class NabuCompileTask : CompileTask {
    override fun execute(context: CompileContext): Boolean {
        val project = context.project

        val nabuFiles = collectNabuFiles(context)

        if (nabuFiles.isEmpty()) {
            return true
        }

        context.addMessage(
            CompilerMessageCategory.INFORMATION,
            "Running Nabu compiler (" + nabuFiles.size + " files)",
            null,
            -1,
            -1
        )

        try {
            val client: NabuCompilerClient = NabuCompilerClient()
            val result: CompileResult = client.compile(nabuFiles)

            for (error in result.getErrors()) {
                context.addMessage(
                    CompilerMessageCategory.ERROR,
                    error.message,
                    error.filePath,
                    error.line,
                    error.column
                )
            }
        } catch (e: Exception) {
            context.addMessage(
                CompilerMessageCategory.ERROR,
                "Nabu compiler failed: " + e.message,
                null,
                -1,
                -1
            )

            return false
        }

        return true
    }

    private fun collectNabuFiles(context: CompileContext): MutableList<VirtualFile?> {
        val result: MutableList<VirtualFile?> = ArrayList<VirtualFile?>()
        ReadAction.run<RuntimeException> {
            for (file in context.compileScope.getFiles(null, true)) {
                if ("nabu".equals(file.extension, ignoreCase = true)) {
                    result.add(file)
                }
            }
            result
        }


        return result
    }
}