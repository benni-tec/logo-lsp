package de.benni_tec.logo_lsp.services.analysis

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.jsonrpc.messages.Either

data class ProcInvocation(
    val name: String,
    val position: Range,
    val args: Int
) {
    fun validate(declaration: ProcDeclaration): List<Diagnostic> {
        val diags = mutableListOf<Diagnostic>()

        if (declaration.params != args) {
            diags += Diagnostic().apply {
                message = Either.forLeft(
                    "Procedure ${declaration.name} expects ${declaration.params} arguments, but got $args",
                )
                range = position
                source = "validator"
            }
        }

        // TODO: check if args are of the correct type

        return diags
    }
}