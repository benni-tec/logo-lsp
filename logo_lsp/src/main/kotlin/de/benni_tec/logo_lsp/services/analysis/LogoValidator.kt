package de.benni_tec.logo_lsp.services.analysis

import de.benni_tec.logo_antlr.logoParser
import de.benni_tec.logo_lsp.utilities.LogoAggregateVisitor
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.util.*

class LogoValidator : LogoAggregateVisitor<Diagnostic>() {
    private val frames = Stack<Frame>()

    companion object {
        fun analyze(prog: logoParser.ProgContext): List<Diagnostic> {
            return LogoValidator().visit(prog)
        }
    }

    override fun visitProg(ctx: logoParser.ProgContext): List<Diagnostic> {
        frames.push(Frame())
        val diags = super.visitProg(ctx)
        frames.pop()
        return diags
    }

    override fun visitProcedureDeclaration(ctx: logoParser.ProcedureDeclarationContext): List<Diagnostic> {
        val diags = mutableListOf<Diagnostic>()

        val name = ctx.name().text

        val procDeclarations = frames.peek().procDeclarations
        if (procDeclarations.containsKey(name)) {
            diags.add(Diagnostic().apply {
                message = Either.forLeft("Procedure ${name} already defined")
                range = range(ctx)
                source = "validator"
            })
        } else {
            procDeclarations[name] = ProcDeclaration(
                name,
                ctx.parameterDeclarations().size,
                range(ctx),
                ctx
            )
        }

        frames.push(Frame())
        diags += super.visitProcedureDeclaration(ctx)
        frames.pop()
        return diags
    }

    override fun visitProcedureInvocation(ctx: logoParser.ProcedureInvocationContext): List<Diagnostic> {
        val diags = mutableListOf<Diagnostic>()

        val name = ctx.name().text

        var decl: ProcDeclaration? = null
        for (frame in frames) {
            decl = frame.procDeclarations[name]
            if (decl != null) break
        }

        if (decl == null) {
            diags += Diagnostic().apply {
                message = Either.forLeft("Procedure ${name} not defined")
                range = range(ctx)
                source = "validator"
            }
        } else {
            diags += ProcInvocation(
                name,
                range(ctx)!!,
                ctx.expression().size,
            ).validate(decl)
        }

        diags += super.visitProcedureInvocation(ctx)
        return diags
    }
}

private data class Frame(
    val procDeclarations: MutableMap<String, ProcDeclaration> = mutableMapOf(),
);

