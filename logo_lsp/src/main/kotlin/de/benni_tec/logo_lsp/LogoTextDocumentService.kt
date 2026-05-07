package de.benni_tec.logo_lsp

import de.benni_tec.logo_lsp.services.AnalysisResult
import de.benni_tec.logo_lsp.services.Analyzer
import de.benni_tec.logo_lsp.services.Highlighter
import org.eclipse.lsp4j.DeclarationParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.ImplementationParams
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.LocationLink
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.jsonrpc.CompletableFutures
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

class LogoTextDocumentService(
    val server: LogoLanguageServer
) : TextDocumentService {
    val documents: HashMap<String, DocumentState> = HashMap()

    // services
    val highlighter = Highlighter()
    val analyzer = Analyzer()

    override fun didOpen(params: DidOpenTextDocumentParams?) {
        if (params == null) return
        receivedDocument(params.textDocument.uri, params.textDocument.text)
    }

    override fun didChange(params: DidChangeTextDocumentParams?) {
        if (params == null) return
        receivedDocument(params.textDocument.uri, params.contentChanges[0].text)
    }

    fun receivedDocument(uri: String, text: String) {
        val analysis = analyzer.analyze(text)
        server.client.publishDiagnostics(PublishDiagnosticsParams(uri, analysis.diagnostics))

        documents[uri] = DocumentState(text, analysis)
    }

    override fun semanticTokensFull(params: SemanticTokensParams?): CompletableFuture<SemanticTokens?>? {
        return CompletableFutures.computeAsync<SemanticTokens?>({
            val document = documents[params?.textDocument?.uri ?: return@computeAsync null] ?: return@computeAsync null
            highlighter.highlight(document.text)
        })
    }

    override fun declaration(params: DeclarationParams?): CompletableFuture<Either<List<Location?>?, List<LocationLink?>?>?>? {
        return CompletableFutures.computeAsync<Either<List<Location?>?, List<LocationLink?>?>>({
            if (params == null) return@computeAsync null
            findDeclaration(params.textDocument.uri, params.position)
        })
    }

    override fun implementation(params: ImplementationParams?): CompletableFuture<Either<List<Location?>?, List<LocationLink?>?>?>? {
        return CompletableFutures.computeAsync<Either<List<Location?>?, List<LocationLink?>?>>({
            if (params == null) return@computeAsync null
            findDeclaration(params.textDocument.uri, params.position)
        })
    }

    /// For LOGO everything is in the same file and the implementation always follows the declaration
    private fun findDeclaration(uri: String, position: Position): Either<List<Location?>?, List<LocationLink?>?>? {
        val document = documents[uri] ?: return null
        val links = document.analysis.findDeclarations(position)
        return Either.forLeft<List<Location?>?, List<LocationLink?>?>(links.map({ range ->
            Location(
                uri,
                range
            )
        }))
    }

    override fun definition(params: DefinitionParams?): CompletableFuture<Either<List<Location?>?, List<LocationLink?>?>?>? {
        return CompletableFutures.computeAsync<Either<List<Location?>?, List<LocationLink?>?>>({
            if (params == null) return@computeAsync null

            val document = documents[params.textDocument.uri] ?: return@computeAsync null
            val links = document.analysis.findDefinitions(params.position)
            Either.forLeft<List<Location?>?, List<LocationLink?>?>(links.map({ range ->
                Location(
                    params.textDocument.uri,
                    range
                )
            }))
        })
    }

    override fun references(params: ReferenceParams?): CompletableFuture<List<Location?>?>? {
        return CompletableFutures.computeAsync<List<Location?>?>({
            if (params == null) return@computeAsync null

            val document = documents[params.textDocument.uri] ?: return@computeAsync null
            val links = document.analysis.findUsages(params.position)
            links.map({ range ->
                Location(
                    params.textDocument.uri,
                    range
                )
            })
        })
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        // nothing to do
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
        // nothing to do
    }

    // release all resources and stop the service
    fun exit() {
        // nothing to do
    }
}

data class DocumentState(
    val text: String,
    val analysis: AnalysisResult,
)
