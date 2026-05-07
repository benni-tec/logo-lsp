package de.benni_tec.logo_lsp.services.analysis

import de.benni_tec.logo_antlr.logoParser
import org.eclipse.lsp4j.Range

data class ProcDeclaration(
    val name: String,
    val params: Int,
    val position: Range?,
    val body: logoParser.ProcedureDeclarationContext
)