package io.github.potjerodekool.nabuintellijplugin.build


class CompileResult(errors: MutableList<CompileError>) {
    private val errors: MutableList<CompileError>

    init {
        this.errors = errors
    }

    fun getErrors(): MutableList<CompileError> {
        return errors
    }
}