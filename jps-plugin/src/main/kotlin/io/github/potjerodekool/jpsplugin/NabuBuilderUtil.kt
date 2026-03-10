package io.github.potjerodekool.jpsplugin

import com.intellij.openapi.util.Key
import com.intellij.util.containers.FileCollectionFactory
import org.jetbrains.jps.incremental.CompileContext
import java.io.File

object NabuBuilderUtil {

    private val SUCCESSFULLY_COMPILED_FILES_KEY: Key<MutableSet<File>> =
        Key.create("_successfully_compiled_files_")

    fun registerSuccessfullyCompiled(context: CompileContext, files: Collection<File>) {
        getFilesContainer(context, SUCCESSFULLY_COMPILED_FILES_KEY).addAll(files)
    }

    private fun getFilesContainer(context: CompileContext, dataKey: Key<MutableSet<File>>): MutableSet<File> {
        return getOrCreate(
            context,
            dataKey,
            FileCollectionFactory::createCanonicalFileSet)
    }

    private fun <T> getOrCreate(
        context: CompileContext,
        dataKey: Key<T>,
        factory: () -> T
    ): T {
        var value = dataKey.get(context, null)

        if (value == null) {
            value = factory()
            dataKey.set(context, value)
        }

        return value
    }
}