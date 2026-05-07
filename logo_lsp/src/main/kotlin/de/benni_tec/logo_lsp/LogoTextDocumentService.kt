package de.benni_tec.logo_lsp

import de.benni_tec.logo_lsp.services.Analyzer
import de.benni_tec.logo_lsp.services.Highlighter
import org.eclipse.lsp4j.DeclarationParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.ImplementationParams
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.LocationLink
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.jsonrpc.CompletableFutures
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

class LogoTextDocumentService(
    val server: LogoLanguageServer
) : TextDocumentService {
    val documents: HashMap<String, String> = HashMap()

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
        documents[uri] = text
        server.client.publishDiagnostics(
            PublishDiagnosticsParams(
                uri,
                analyzer.analyze(text),
            )
        )
    }

    override fun semanticTokensFull(params: SemanticTokensParams?): CompletableFuture<SemanticTokens?>? {
        return CompletableFutures.computeAsync<SemanticTokens?>({
            val document = documents[params?.textDocument?.uri ?: return@computeAsync null] ?: return@computeAsync null
            highlighter.highlight(document)
        })
    }

    override fun declaration(params: DeclarationParams?): CompletableFuture<Either<List<Location?>?, List<LocationLink?>?>?>? {
        TODO()
    }

    override fun implementation(params: ImplementationParams?): CompletableFuture<Either<List<Location?>?, List<LocationLink?>?>?>? {
        TODO()
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
