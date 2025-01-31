package raft.war.jass.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.*
import raft.war.jass.lsp.service.document.JassTextDocumentService
import raft.war.jass.lsp.service.JassWorkspaceService
import raft.war.jass.lsp.token.TokenModifier
import raft.war.jass.lsp.token.TokenType
import java.util.concurrent.CompletableFuture

class JassLanguageServer(val args: Array<String>) : LanguageServer, LanguageClientAware {
    private val workspaceService = JassWorkspaceService()
    private val textDocumentService = JassTextDocumentService(this)

    var client: LanguageClient? = null

    override fun connect(client: LanguageClient) {
        this.client = client
    }

    @Suppress("unused")
    fun log(message: String) = client?.logMessage(MessageParams(MessageType.Log, message))

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult?> {
        val capabilities = ServerCapabilities().apply {
            textDocumentSync = Either.forRight(TextDocumentSyncOptions().apply {
                openClose = true
                change = TextDocumentSyncKind.Full
            })

            completionProvider = CompletionOptions().apply {
                resolveProvider = true
                triggerCharacters = listOf(".", "(")
            }

            // https://code.visualstudio.com/api/language-extensions/semantic-highlight-guide
            semanticTokensProvider = SemanticTokensWithRegistrationOptions().apply {
                legend = SemanticTokensLegend(
                    TokenType.entries.map { it -> it.name.lowercase() },
                    TokenModifier.entries.map { it -> it.name.lowercase() },
                )

                full = Either.forLeft(true)
            }

            documentHighlightProvider = Either.forLeft(true)

            foldingRangeProvider = Either.forLeft(true)

            diagnosticProvider = DiagnosticRegistrationOptions().apply {

            }
        }
        return CompletableFuture.completedFuture(InitializeResult(capabilities))
    }

    override fun shutdown(): CompletableFuture<Any> = CompletableFuture.completedFuture(null)
    override fun exit() = Unit
    override fun getTextDocumentService(): TextDocumentService = textDocumentService
    override fun getWorkspaceService(): WorkspaceService = workspaceService
    override fun setTrace(params: SetTraceParams?) = Unit
}
