package de.benni_tec.logo_lsp.services.analysis

import de.benni_tec.logo_antlr.logoParser
import de.benni_tec.logo_lsp.services.AnalysisResult
import de.benni_tec.logo_lsp.utilities.LogoAggregateVisitor
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.util.*

class LogoAnalysisVisitor : LogoAggregateVisitor<Diagnostic>() {
    private val frames = Stack<Frame>()

    private val variablesDeclToUsage = mutableMapOf<Range, MutableList<Range>>()
    private val variablesUsageToDecl = mutableMapOf<Range, MutableList<Range>>()
    private val variablesUsageToDef = mutableMapOf<Range, MutableList<Range>>()

    private val proceduresDeclToUsage = mutableMapOf<Range, MutableList<Range>>()
    private val proceduresUsageToDecl = mutableMapOf<Range, MutableList<Range>>()

    fun analyze(prog: logoParser.ProgContext): AnalysisResult {
        return AnalysisResult(
            visit(prog),
            PositionMapping(
                proceduresDeclToUsage,
                proceduresUsageToDecl,
                proceduresUsageToDecl,
            ),
            PositionMapping(
                variablesDeclToUsage,
                variablesUsageToDecl,
                variablesUsageToDef,
            ),
        )
    }

    override fun visitProg(ctx: logoParser.ProgContext): List<Diagnostic> {
        // add the initial frame
        frames.push(Frame())
        val diags = super.visitProg(ctx)
        frames.pop()
        return diags
    }

    /**
     * Gathers all procedure declarations and their parameters.
     * Adds a frame for the procedure body.
     * */
    override fun visitProcedureDeclaration(ctx: logoParser.ProcedureDeclarationContext): List<Diagnostic> {
        val diags = mutableListOf<Diagnostic>()

        val name = ctx.name().text
        val params = ctx.parameterDeclarations().flatten()

        val frame = frames.peek()
        val procDeclarations = frame.procDeclarations
        if (procDeclarations.containsKey(name)) {
            diags.add(Diagnostic().apply {
                message = Either.forLeft("Procedure ${name} already defined")
                range = range(ctx)
                source = "validator"
            })
        } else {
            procDeclarations[name] = ProcDeclaration(
                name,
                params.size,
                range(ctx.name()),
                ctx
            )
        }

        // add the parameters as variables within the procedure body
        val procFrame = Frame()
        for (param in params) {
            val paramName = param.name().text
            procFrame.varDeclarations[paramName] = VarDeclaration(paramName, range(param))
            procFrame.varDefinitions[paramName] = VarDefinition(paramName, range(param))
        }

        frames.push(procFrame)
        diags += super.visitProcedureDeclaration(ctx)
        frames.pop()
        return diags
    }

    /**
     * Validates the procedure invocation against the procedure declaration.
     * */
    override fun visitProcedureInvocation(ctx: logoParser.ProcedureInvocationContext): List<Diagnostic> {
        val diags = mutableListOf<Diagnostic>()

        val name = ctx.name().text
        val position = range(ctx.name())

        val decl = frames.findProcDeclaration(name)
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

            if (decl.position != null && position != null) {
                proceduresDeclToUsage.add(decl.position, position)
                proceduresUsageToDecl.add(position, decl.position)
            }
        }

        return diags + super.visitProcedureInvocation(ctx)
    }

    /**
     * Gathers all variable declarations.
     * */
    override fun visitMake(ctx: logoParser.MakeContext): List<Diagnostic> {
        val varName = ctx.STRINGLITERAL().text.trimStart('"')
        val varRange = range(ctx.STRINGLITERAL().symbol)

        val frame = frames.peek()
        frame.varDefinitions[varName] = VarDefinition(varName, varRange)
        frame.varDeclarations.putIfAbsent(varName, VarDeclaration(varName, varRange))

        return super.visitMake(ctx)
    }

    /**
     * Validates the variable dereference against the variable declaration.
     * */
    override fun visitDeref(ctx: logoParser.DerefContext): List<Diagnostic> {
        val diags = mutableListOf<Diagnostic>()

        val varName = ctx.name().text
        val varRange = range(ctx)!!

        // check if the variable is declared
        val decl = frames.findVarDeclaration(varName)
        if (decl == null) {
            diags += Diagnostic().apply {
                message = Either.forLeft("Variable $varName not declared")
                range = varRange
                source = "validator"
            }
        } else if (decl.position != null) {
            variablesUsageToDecl.add(varRange, decl.position)
            variablesDeclToUsage.add(decl.position, varRange)

            // check where the last definition of the variable is
            val def = frames.findVarDefinition(varName)
            if (def == null) {
                diags += Diagnostic().apply {
                    message = Either.forLeft("Variable $varName not defined")
                    range = varRange
                    source = "validator"
                }
            } else if (def.position != null) {
                variablesUsageToDef.add(varRange, def.position)
                if (def.position != decl.position) variablesDeclToUsage.add(def.position, varRange)
            }
        }

        return diags + super.visitDeref(ctx)
    }

    fun <K, V> MutableMap<K, MutableList<V>>.add(key: K, value: V) {
        val list = this[key] ?: mutableListOf<V>().also { this[key] = it }
        list.add(value)
    }
}

private data class Frame(
    val procDeclarations: MutableMap<String, ProcDeclaration> = mutableMapOf(),

    val varDeclarations: MutableMap<String, VarDeclaration> = mutableMapOf(),
    val varDefinitions: MutableMap<String, VarDefinition> = mutableMapOf(),
)

private fun Iterable<Frame>.findProcDeclaration(name: String): ProcDeclaration? {
    return this.firstNotNullOfOrNull { it.procDeclarations[name] }
}

private fun Iterable<Frame>.findVarDeclaration(name: String): VarDeclaration? {
    return this.firstNotNullOfOrNull { it.varDeclarations[name] }
}

private fun Iterable<Frame>.findVarDefinition(name: String): VarDefinition? {
    return this.firstNotNullOfOrNull { it.varDefinitions[name] }
}

fun List<logoParser.ParameterDeclarationsContext>.flatten(): List<logoParser.ParameterDeclarationsContext> {
    return this.flatMap { it.flatten() }
}

fun logoParser.ParameterDeclarationsContext.flatten(): List<logoParser.ParameterDeclarationsContext> {
    val result = mutableListOf<logoParser.ParameterDeclarationsContext>(this)
    for (param in this.parameterDeclarations()) {
        result += param.flatten()
    }
    return result
}
