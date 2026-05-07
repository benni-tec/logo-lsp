package de.benni_tec.logo_lsp.services

import de.benni_tec.logo_antlr.logoLexer
import de.benni_tec.logo_antlr.logoParser
import de.benni_tec.logo_lsp.services.analysis.LogoAnalysisVisitor
import de.benni_tec.logo_lsp.services.analysis.PositionMapping
import org.antlr.v4.kotlinruntime.*
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

class Analyzer {
    fun analyze(document: String): AnalysisResult {
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
        if (diagnostics.isNotEmpty()) {
            return AnalysisResult(diagnostics, PositionMapping(), PositionMapping())
        }

        val analysis = LogoAnalysisVisitor().analyze(prog)
        return AnalysisResult(
            analysis.diagnostics + diagnostics,
            analysis.procedureMapping,
            analysis.variableMapping,
        )
    }
}

data class AnalysisResult(
    val diagnostics: List<Diagnostic>,
    val procedureMapping: PositionMapping,
    val variableMapping: PositionMapping,
) {
    fun findUsages(position: Position): List<Range> {
        return procedureMapping.findUsages(position) + variableMapping.findUsages(position)
    }

    fun findDeclarations(position: Position): List<Range> {
        return procedureMapping.findDeclarations(position) + variableMapping.findDeclarations(position)
    }

    fun findDefinitions(position: Position): List<Range> {
        return procedureMapping.findDefinitions(position) + variableMapping.findDefinitions(position)
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
