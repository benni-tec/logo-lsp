package de.benni_tec.logo_lsp

import de.benni_tec.logo_lsp.services.highlight.SemanticToken
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.ReferenceOptions
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.jsonrpc.CompletableFutures
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture

class LogoLanguageServer : LanguageServer, LanguageClientAware {
    lateinit var client: LanguageClient
    private val textDocumentService: LogoTextDocumentService = LogoTextDocumentService(this)

    override fun connect(client: LanguageClient) {
        this.client = client
    }

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult?>? {
        return CompletableFutures.computeAsync<InitializeResult>({
            val caps = ServerCapabilities().apply {
                textDocumentSync = Either.forLeft(TextDocumentSyncKind.Full)
                semanticTokensProvider = SemanticTokensWithRegistrationOptions().apply {
                    legend = SemanticToken.legend()
                    full = Either.forLeft(true)
                }
                referencesProvider = Either.forLeft(true)
                declarationProvider = Either.forLeft(true)
                definitionProvider = Either.forLeft(true)
                implementationProvider = Either.forLeft(true)
            }

            InitializeResult(caps)
        })
    }

    override fun shutdown(): CompletableFuture<in Any>? {
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {
        textDocumentService.exit()
    }

    override fun getTextDocumentService(): TextDocumentService {
        return textDocumentService
    }

    override fun getWorkspaceService(): WorkspaceService? {
        return null
    }
}