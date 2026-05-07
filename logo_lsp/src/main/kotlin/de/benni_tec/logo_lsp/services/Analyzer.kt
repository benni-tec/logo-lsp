package de.benni_tec.logo_lsp.services

import de.benni_tec.logo_antlr.logoLexer
import de.benni_tec.logo_antlr.logoParser
import de.benni_tec.logo_lsp.services.analysis.LogoValidator
import org.antlr.v4.kotlinruntime.*
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

class Analyzer {
    fun analyze(document: String): List<Diagnostic> {
        val diagnostics = mutableListOf<Diagnostic>()

        val input = CharStreams.fromString(document)
        val lexer = logoLexer(input)

        lexer.removeErrorListeners()
        lexer.addErrorListener(LogoSyntaxErrorListener(diagnostics, "lexer"))

        val tokens = CommonTokenStream(lexer)
        val parser = logoParser(tokens)

        parser.removeErrorListeners()
        parser.addErrorListener(LogoSyntaxErrorListener(diagnostics, "parser"))

        // parse the whole document
        val prog = parser.prog()

        // only run semantic analysis if there are no syntax errors
        if (diagnostics.isEmpty()) {
            diagnostics.addAll(LogoValidator.analyze(prog))
        }

        return diagnostics
    }
}

private class LogoSyntaxErrorListener(
    private val diagnostics: MutableList<Diagnostic>,
    private val source: String
) : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        val start = Position(line - 1, charPositionInLine)
        val end = Position(line - 1, charPositionInLine + 1)

        diagnostics += Diagnostic(
            Range(start, end),
            msg,
            DiagnosticSeverity.Error,
            source
        )
    }
}
