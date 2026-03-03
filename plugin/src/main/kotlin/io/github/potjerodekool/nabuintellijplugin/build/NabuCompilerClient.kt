package io.github.potjerodekool.nabuintellijplugin.build

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.net.Socket

class NabuCompilerClient {
    private val mapper = ObjectMapper()

    @Throws(IOException::class)
    fun compile(files: MutableList<VirtualFile?>): CompileResult {
        Socket(HOST, PORT).use { socket ->
            val out = socket.getOutputStream()
            val `in` = socket.getInputStream()

            // Stuur lijst van file paths
            mapper.writeValue(
                out,
                files.stream()
                    .map { obj: VirtualFile? -> obj!!.getPath() }
                    .toList()
            )

            out.flush()

            // Lees resultaat
            val errors: Array<CompileError> =
                mapper.readValue(`in`, Array<CompileError>::class.java)
            return CompileResult(mutableListOf(*errors))
        }
    }

    companion object {
        private const val HOST = "localhost"
        private const val PORT = 5555
    }
}