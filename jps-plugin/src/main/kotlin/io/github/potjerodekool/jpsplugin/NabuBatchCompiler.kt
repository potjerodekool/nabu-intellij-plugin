package io.github.potjerodekool.jpsplugin

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.IOException
import java.net.Socket


object NabuBatchCompiler {
    @Throws(IOException::class)
    fun compile(files: MutableList<File?>) {
        Socket("localhost", 5555).use { socket ->
            val mapper = ObjectMapper()
            mapper.writeValue(
                socket.getOutputStream(),
                files.stream().map<String?> { obj: File? -> obj!!.getAbsolutePath() }.toList()
            )
            socket.getOutputStream().flush()
        }
    }
}