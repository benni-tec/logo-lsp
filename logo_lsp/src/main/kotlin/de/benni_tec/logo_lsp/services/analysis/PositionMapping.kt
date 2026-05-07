package de.benni_tec.logo_lsp.services.analysis

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

data class PositionMapping(
    private val declToUsage: Map<Range, List<Range>> = mapOf(),
    private val usageToDecl: Map<Range, List<Range>> = mapOf(),
    private val usageToDef:  Map<Range, List<Range>> = mapOf(),
) {
    fun findUsages(position: Position): List<Range> {
        return find(declToUsage, position)
    }

    fun findDeclarations(position: Position): List<Range> {
        return find(usageToDecl, position)
    }

    fun findDefinitions(position: Position): List<Range> {
        return find(usageToDef, position)
    }

    // TODO: better spacial structure?!
    private fun find(map: Map<Range, List<Range>>, position: Position): List<Range> {
        val result = mutableListOf<Range>()
        for ((range, targets) in map) {
            if (range.contains(position)) {
                result.addAll(targets)
            }
        }

        return result
    }

    private fun Range.contains(position: Position): Boolean {
        if (start.line == end.line) {
            return start.line == position.line && start.character <= position.character && end.character >= position.character
        }

        return when (position.line) {
            start.line -> position.character >= start.character
            end.line -> position.character <= end.character
            else -> false
        }
    }
}
