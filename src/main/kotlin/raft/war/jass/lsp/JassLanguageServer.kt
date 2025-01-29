package raft.war.jass.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.*
import java.util.concurrent.CompletableFuture

class JassLanguageServer : LanguageServer, LanguageClientAware {
    private val workspaceService = JassWorkspaceService()
    private val textDocumentService = JassDocumentService(this)

    var client: LanguageClient? = null

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult?> {
        return try {
            val capabilities = ServerCapabilities().apply {
                textDocumentSync = Either.forRight(TextDocumentSyncOptions().apply {
                    openClose = true
                    change = TextDocumentSyncKind.Incremental
                })

                completionProvider = CompletionOptions().apply {
                    resolveProvider = true
                    triggerCharacters = listOf(".", "(")
                }

                semanticTokensProvider = SemanticTokensWithRegistrationOptions().apply {
                    legend = SemanticTokensLegend(
                        listOf("keyword", "variable", "string", "function", "class"),
                        listOf("declaration", "definition", "readonly", "deprecated")
                    )
                    full = Either.forLeft(true)
                }

                documentHighlightProvider = Either.forLeft(true)
            }
            CompletableFuture.completedFuture(InitializeResult(capabilities))
        } catch (_: Exception) {
            CompletableFuture.completedFuture(null)
        }
    }

    override fun shutdown(): CompletableFuture<Any> = CompletableFuture.completedFuture(null)
    override fun exit() = Unit
    override fun getTextDocumentService(): TextDocumentService = textDocumentService
    override fun getWorkspaceService(): WorkspaceService = workspaceService


    override fun setTrace(params: SetTraceParams?) = Unit
    override fun connect(client: LanguageClient) {
        this.client = client
    }
}
