package de.benni_tec.logo_lsp

import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClientAware

fun main() {
    val languageServer = LogoLanguageServer()
    val launcher = LSPLauncher.createServerLauncher(languageServer, System.`in`, System.out)

    if (languageServer is LanguageClientAware) {
        languageServer.connect(launcher.remoteProxy)
    }

    launcher.startListening()
}