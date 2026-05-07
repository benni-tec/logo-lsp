package de.benni_tec.logo_lsp.services.highlight

import de.benni_tec.logo_antlr.logoParser
import de.benni_tec.logo_lsp.utilities.LogoAggregateVisitor
import kotlin.collections.plus

class SemanticTokenVisitor : LogoAggregateVisitor<SemanticToken>() {
    override fun visitProcedureDeclaration(ctx: logoParser.ProcedureDeclarationContext): List<SemanticToken> {
        return super.visitProcedureDeclaration(ctx) + SemanticToken(
            range(ctx.name())!!,
            SemanticTokenType.FUNCTION,
            SemanticTokenModifier.DECLARATION
        )
    }

    override fun visitProcedureInvocation(ctx: logoParser.ProcedureInvocationContext): List<SemanticToken> {
        return super.visitProcedureInvocation(ctx) + SemanticToken(
            range(ctx.name())!!,
            SemanticTokenType.FUNCTION,
        )
    }
}