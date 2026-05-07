package de.benni_tec.logo_lsp.services.highlight

import de.benni_tec.logo_antlr.logoLexer
import de.benni_tec.logo_lsp.services.highlight.SemanticTokenModifier.Companion.toBitMask
import de.benni_tec.logo_lsp.services.highlight.SemanticTokenType.COMMENT
import de.benni_tec.logo_lsp.services.highlight.SemanticTokenType.FUNCTION
import de.benni_tec.logo_lsp.services.highlight.SemanticTokenType.KEYWORD
import de.benni_tec.logo_lsp.services.highlight.SemanticTokenType.NUMBER
import de.benni_tec.logo_lsp.services.highlight.SemanticTokenType.OPERATOR
import de.benni_tec.logo_lsp.services.highlight.SemanticTokenType.STRING
import org.antlr.v4.kotlinruntime.Token
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SemanticTokensLegend

fun String.camelCase() = split("_")
    .mapIndexed({ i, s -> if (i == 0) s.lowercase() else s.lowercase().capitalize() })
    .joinToString("")

data class SemanticToken(
    val range: Range,
    val type: SemanticTokenType,
    val modifier: List<SemanticTokenModifier>
) {
    constructor(
        range: Range,
        type: SemanticTokenType,
        vararg modifiers: SemanticTokenModifier
    ) : this(range, type, modifiers.toList())

    companion object {
        fun legend(): SemanticTokensLegend {
            return SemanticTokensLegend(
                SemanticTokenType.legend(),
                SemanticTokenModifier.legend(),
            )
        }

        fun from(token: Token): SemanticToken? {
            val range = Range(
                Position(token.line - 1, token.charPositionInLine),
                Position(token.line - 1, token.charPositionInLine + (token.text?.length ?: 0)),
            )

            return when (token.type) {
                logoLexer.Tokens.T__0,  // to
                logoLexer.Tokens.T__1,  // end
                logoLexer.Tokens.T__4,  // repeat
                logoLexer.Tokens.T__7,  // if
                logoLexer.Tokens.T__11, // make
                    -> SemanticToken(range, KEYWORD)

                logoLexer.Tokens.T__12, // print
                logoLexer.Tokens.T__17, // fd
                logoLexer.Tokens.T__18, // forward
                logoLexer.Tokens.T__19, // bk
                logoLexer.Tokens.T__20, // backward
                logoLexer.Tokens.T__21, // rt
                logoLexer.Tokens.T__22, // right
                logoLexer.Tokens.T__23, // lt
                logoLexer.Tokens.T__24, // left
                logoLexer.Tokens.T__25, // cs
                logoLexer.Tokens.T__26, // clearscreen
                logoLexer.Tokens.T__27, // pu
                logoLexer.Tokens.T__28, // penup
                logoLexer.Tokens.T__29, // pd
                logoLexer.Tokens.T__30, // pendown
                logoLexer.Tokens.T__31, // ht
                logoLexer.Tokens.T__32, // hideturtle
                logoLexer.Tokens.T__33, // st
                logoLexer.Tokens.T__34, // showturtle
                logoLexer.Tokens.T__35, // home
                logoLexer.Tokens.T__36, // stop
                logoLexer.Tokens.T__37, // label
                logoLexer.Tokens.T__38, // setxy
                logoLexer.Tokens.T__39, // random
                logoLexer.Tokens.T__40, // for
                    -> SemanticToken(range, FUNCTION, SemanticTokenModifier.DEFAULT_LIBRARY)

                logoLexer.Tokens.NUMBER -> SemanticToken(range, NUMBER)
                logoLexer.Tokens.STRINGLITERAL -> SemanticToken(range,STRING)
                logoLexer.Tokens.COMMENT -> SemanticToken(range, COMMENT)

                logoLexer.Tokens.T__8,  // <
                logoLexer.Tokens.T__9,  // >
                logoLexer.Tokens.T__10, // =
                logoLexer.Tokens.T__13, // +
                logoLexer.Tokens.T__14, // -
                logoLexer.Tokens.T__15, // *
                logoLexer.Tokens.T__16, // /
                    -> SemanticToken(range, OPERATOR)

                else -> null
            }
        }

        fun Iterable<SemanticToken>.encode(document: String): List<Int> {
            // do not use lines, so we include \r in the length calculation
            val lines = document.lines()
            fun Range.length(): Int {
                if (start.line == end.line) return end.character - start.character

                var sum = 0
                for (i in start.line until end.line) {
                    sum += lines[i].length
                }

                sum -= start.character
                sum += end.character
                return sum
            }

            val data = mutableListOf<Int>()

            var previousLine = 0
            var previousStart = 0
            for (token in this) {
                val line = token.range.start.line
                val start = token.range.start.character
                val length = token.range.length()

                val deltaLine = line - previousLine
                val deltaStart = if (deltaLine == 0) {
                    start - previousStart
                } else {
                    start
                }

                data += deltaLine
                data += deltaStart
                data += length
                data += token.type.toLegendValue()
                data += token.modifier.toBitMask()

                previousLine = line
                previousStart = start
            }

            return data
        }
    }
}

enum class SemanticTokenModifier {
    DECLARATION,
    DEFINITION,
    READONLY,
    STATIC,
    DEPRECATED,
    ABSTRACT,
    ASYNC,
    MODIFICATION,
    DOCUMENTATION,
    DEFAULT_LIBRARY;

    fun toBitMask(): Int {
        return 1.shl(mapping[this]!!)
    }

    companion object {
        private val mapping = entries.withIndex().associateBy({ it.value }, { it.index })

        fun legend(): List<String> {
            return entries.map { it.name.camelCase() }
        }

        fun Iterable<SemanticTokenModifier>.toBitMask(): Int = fold(0) { acc, modifier -> acc or modifier.toBitMask() }
    }
}

enum class SemanticTokenType {
    KEYWORD,
    NUMBER,
    STRING,
    COMMENT,
    FUNCTION,
    VARIABLE,
    OPERATOR;

    fun toLegendValue(): Int {
        return mapping[this]!!
    }

    companion object {
        private val mapping = entries.withIndex().associateBy({ it.value }, { it.index })

        fun legend(): List<String> {
            return entries.map { it.name.lowercase() }
        }
    }
}