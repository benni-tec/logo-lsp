package de.benni_tec.logo_lsp

import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture

class LogoLanguageServer : LanguageServer, LanguageClientAware {
    private lateinit var client: LanguageClient
    private val textDocumentService: LogoTextDocumentService = LogoTextDocumentService()

    override fun connect(client: LanguageClient) {
        this.client = client
    }

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult?>? {
        return CompletableFuture.completedFuture(InitializeResult())
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