package de.benni_tec.logo_lsp.services.analysis

import org.eclipse.lsp4j.Range

data class VarDeclaration(
    val name: String,
    val position: Range?
)

data class VarDefinition(
    val name: String,
    val position: Range?
)

data class VarUsage(
    val name: String,
    val position: Range
)