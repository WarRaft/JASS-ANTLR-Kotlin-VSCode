package raft.war.jass.lsp

import org.eclipse.lsp4j.CompletionOptions
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.SetTraceParams
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.TextDocumentSyncOptions
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture

class JassLanguageServer : LanguageServer, LanguageClientAware {
    private val documentService = JassDocumentService()
    private val workspaceService = JassWorkspaceService()

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
            }
            CompletableFuture.completedFuture(InitializeResult(capabilities))
        } catch (_: Exception) {
            CompletableFuture.completedFuture(null)
        }
    }

    override fun shutdown(): CompletableFuture<Any> = CompletableFuture.completedFuture(null)
    override fun exit() = Unit
    override fun getTextDocumentService(): TextDocumentService = documentService
    override fun getWorkspaceService(): WorkspaceService = workspaceService
    override fun connect(p0: LanguageClient?) = Unit
    override fun setTrace(params: SetTraceParams?) = Unit
}
