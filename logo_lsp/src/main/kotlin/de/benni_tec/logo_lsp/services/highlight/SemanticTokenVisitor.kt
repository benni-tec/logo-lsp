package de.benni_tec.logo_lsp.services.highlight

import de.benni_tec.logo_antlr.logoParser
import de.benni_tec.logo_lsp.utilities.LogoAggregateVisitor
import kotlin.collections.plus

class SemanticTokenVisitor : LogoAggregateVisitor<SemanticToken>() {
    override fun visitProcedureDeclaration(ctx: logoParser.ProcedureDeclarationContext): List<SemanticToken> {
        val semanticTokens = mutableListOf<SemanticToken>()
        semanticTokens += SemanticToken(
            range(ctx.name())!!,
            SemanticTokenType.FUNCTION,
            SemanticTokenModifier.DECLARATION,
            SemanticTokenModifier.DEFINITION,
        )

        for (param in ctx.parameterDeclarations()) {
            semanticTokens += SemanticToken(
                range(param)!!,
                SemanticTokenType.VARIABLE,
                SemanticTokenModifier.DECLARATION,
                SemanticTokenModifier.DEFINITION,
            )
        }

        return semanticTokens + super.visitProcedureDeclaration(ctx)
    }

    override fun visitProcedureInvocation(ctx: logoParser.ProcedureInvocationContext): List<SemanticToken> {
        return super.visitProcedureInvocation(ctx) + SemanticToken(
            range(ctx.name())!!,
            SemanticTokenType.FUNCTION,
        )
    }

    override fun visitMake(ctx: logoParser.MakeContext): List<SemanticToken> {
        return listOf(SemanticToken(
            range(ctx.STRINGLITERAL().symbol),
            SemanticTokenType.VARIABLE,
            SemanticTokenModifier.DEFINITION,
        )) + super.visitMake(ctx)
    }

    override fun visitDeref(ctx: logoParser.DerefContext): List<SemanticToken> {
        return listOf(SemanticToken(
            range(ctx)!!,
            SemanticTokenType.VARIABLE,
        )) + super.visitDeref(ctx)
    }
}
