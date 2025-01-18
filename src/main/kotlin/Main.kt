import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.launch.LSPLauncher.createServerLauncher
import org.eclipse.lsp4j.services.*
import java.util.concurrent.CompletableFuture

class MyLanguageServer : LanguageServer, LanguageClientAware {
    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult?> {
        return try {
            val capabilities = ServerCapabilities().apply {
                textDocumentSync = Either.forRight(TextDocumentSyncOptions().apply {
                    openClose = true
                    change = TextDocumentSyncKind.Incremental
                })
            }
            CompletableFuture.completedFuture(InitializeResult(capabilities))
        } catch (e: Exception) {
            e.printStackTrace()
            CompletableFuture.completedFuture(null)
        }
    }

    override fun shutdown(): CompletableFuture<Any> {
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {
    }

    override fun getTextDocumentService(): TextDocumentService? {
        return null
    }

    override fun getWorkspaceService(): WorkspaceService? {
        return null
    }

    override fun connect(p0: LanguageClient?) {
    }
}


fun main() {
    try {
        val input = System.`in`
        val output = System.out
        val server = MyLanguageServer()
        val launcher = createServerLauncher(server, input, output)
        launcher.startListening()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
