package raft.war.jass.lsp

import io.github.warraft.jass.antlr.JassState
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Token
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.DocumentHighlight
import org.eclipse.lsp4j.DocumentHighlightKind
import org.eclipse.lsp4j.DocumentHighlightParams
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

class JassDocumentService(val server: JassLanguageServer) : TextDocumentService {
    var documents: HashMap<String?, TextDocumentItem?> = HashMap<String?, TextDocumentItem?>()
    private val states = mutableMapOf<String, JassState>()

    override fun documentHighlight(params: DocumentHighlightParams?): CompletableFuture<List<DocumentHighlight>> {
        val highlights = mutableListOf<DocumentHighlight>()
        val position = params?.position

        if (position != null && false) {
            highlights.add(
                DocumentHighlight(
                    Range(
                        Position(0, 5),
                        Position(0, 10)
                    ),
                    DocumentHighlightKind.Read
                )
            )

            highlights.add(
                DocumentHighlight(
                    Range(
                        Position(2, 3),
                        Position(2, 8)
                    ),
                    DocumentHighlightKind.Write
                )
            )
        }

        return CompletableFuture.completedFuture(highlights)
    }

    override fun semanticTokensFull(params: SemanticTokensParams?): CompletableFuture<SemanticTokens> {
        // https://code.visualstudio.com/api/language-extensions/semantic-highlight-guide


        val uri = params?.textDocument?.uri ?: return CompletableFuture.completedFuture(SemanticTokens())

        val state = states[uri]
        if (state == null) return CompletableFuture.completedFuture(SemanticTokens())

        val data = mutableListOf<Int>()

        if (false) {
            data.add(0) // строка
            data.add(2) // начальная позиция
            data.add(10) // длина токена
            data.add(2) // индекс типа (0 = "keyword")
            data.add(0) // модификаторы (нет модификаторов)

            return CompletableFuture.completedFuture(SemanticTokens(data))
        }

        fun add(t: Token) {
            data.add(t.line - 1)
            data.add(t.charPositionInLine)
            data.add(t.text.length)
            data.add(0) // индекс типа (0 = "keyword")
            data.add(0) // модификаторы (нет модификаторов)
        }

        for (f in state.functions) {
            val c = f.ctx
            if (c == null) continue

            add(c.FUNCTION().symbol)
            add(c.ENDFUNCTION().symbol)
        }

        return CompletableFuture.completedFuture(SemanticTokens(data))
    }

    override fun completion(params: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
        val completionItems = listOf(
            CompletionItem("Anal5"),
            CompletionItem("Cunt4")
        )
        return CompletableFuture.completedFuture(Either.forLeft(completionItems))
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover> {
        val hover = Hover(listOf(Either.forLeft("Anal22")), null)
        return CompletableFuture.completedFuture(hover)
    }

    private fun stateUpdate(item: TextDocumentItem?) {
        if (item == null) return
        val uri = item.uri ?: return
        val text = item.text
        val s = JassState()
        states[uri] = s
        s.parse(CharStreams.fromString(text))
    }

    override fun didOpen(params: DidOpenTextDocumentParams) {
        server.client?.logMessage(MessageParams(MessageType.Log, "didOpen"))

        documents.put(params.textDocument.uri, params.textDocument)
        stateUpdate(params.textDocument)
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        server.client?.logMessage(MessageParams(MessageType.Log, "didClose"))
        val uri = params.textDocument.uri
        documents.remove(uri)
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        server.client?.logMessage(MessageParams(MessageType.Log, "didChange"))

        val uri = params.textDocument.uri
        for (changeEvent in params.contentChanges) {
            if (changeEvent.range != null) {
                throw UnsupportedOperationException("Range should be null for full document update.")
            }
            documents.get(uri)?.text = changeEvent.text
        }

        stateUpdate(documents[params.textDocument.uri])
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
    }

    override fun resolveCompletionItem(unresolved: CompletionItem?): CompletableFuture<CompletionItem?>? {
        return CompletableFuture.completedFuture(unresolved)
    }
}


