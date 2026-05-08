package de.benni_tec.logo_lsp.utilities

import de.benni_tec.logo_antlr.logoBaseVisitor
import org.antlr.v4.kotlinruntime.ParserRuleContext
import org.antlr.v4.kotlinruntime.Token
import org.antlr.v4.kotlinruntime.ast.Point
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

/**
 * Base class for Logo visitors that aggregate results into lists.
 * It also provides some helper methods to get the lsp ranges from antlr tokens and rules.
 * */
open class LogoAggregateVisitor<T> : logoBaseVisitor<List<T>>() {
    override fun aggregateResult(aggregate: List<T>, nextResult: List<T>): List<T> {
        return aggregate + nextResult
    }

    override fun defaultResult(): List<T> {
        return emptyList()
    }

    protected fun range(tree: ParserRuleContext): Range? {
        if (tree.position == null) return null
        return range(tree.position!!.start, tree.position!!.end)
    }

    protected fun range(token: Token): Range {
        return range(token.startPoint(), token.endPoint())
    }

    protected fun range(start: Point, end: Point?): Range {
        return Range(
            Position(start.line - 1, start.column),
            Position((end?.line ?: start.line) - 1, end?.column ?: (start.column + 1)),
        )
    }
}