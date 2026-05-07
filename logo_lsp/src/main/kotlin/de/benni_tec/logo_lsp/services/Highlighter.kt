package de.benni_tec.logo_lsp.services

import de.benni_tec.logo_antlr.logoLexer
import de.benni_tec.logo_antlr.logoParser
import de.benni_tec.logo_antlr.logoParser.Tokens.EOF
import de.benni_tec.logo_lsp.services.highlight.SemanticToken.Companion.encode
import de.benni_tec.logo_lsp.services.highlight.SemanticToken
import de.benni_tec.logo_lsp.services.highlight.SemanticTokenVisitor
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.eclipse.lsp4j.SemanticTokens

class Highlighter {
    fun highlight(document: String): SemanticTokens {
        val input = CharStreams.fromString(document)
        val lexer = logoLexer(input)

        val tokens = CommonTokenStream(lexer)
        val parser = logoParser(tokens)

        val lexerTokens = mutableListOf<SemanticToken>()
        while (true) {
            val token = lexer.nextToken()
            if (token.type == EOF) break

            val semantic = SemanticToken.from(token)
            if (semantic != null) lexerTokens.add(semantic)
        }

        val semanticTokens = lexerTokens + SemanticTokenVisitor().visit(parser.prog())
        return SemanticTokens(semanticTokens.encode(document))
    }
}