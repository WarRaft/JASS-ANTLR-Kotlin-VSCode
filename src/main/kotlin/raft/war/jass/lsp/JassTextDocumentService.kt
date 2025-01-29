package raft.war.jass.lsp

import io.github.warraft.jass.antlr.JassParser
import io.github.warraft.jass.antlr.JassParser.ParamContext
import io.github.warraft.jass.antlr.JassParser.VariableContext
import io.github.warraft.jass.antlr.JassState
import io.github.warraft.jass.antlr.psi.IJassNode
import org.antlr.v4.runtime.CharStreams
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import raft.war.jass.lsp.token.TokenHub
import raft.war.jass.lsp.token.TokenModifier
import raft.war.jass.lsp.token.TokenType
import java.util.concurrent.CompletableFuture

class JassTextDocumentService(val server: JassLanguageServer) : TextDocumentService {
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

    fun semanticStmt(nodes: List<IJassNode>, hub: TokenHub) {

    }

    override fun semanticTokensFull(params: SemanticTokensParams?): CompletableFuture<SemanticTokens> {
        val state = states[params?.textDocument?.uri]
        if (state == null) return CompletableFuture.completedFuture(SemanticTokens())

        val hub = TokenHub()

        for (f in state.functions) {
            val c = f.ctx
            if (c == null) continue

            hub.add(c.FUNCTION(), TokenType.KEYWORD)
            hub.add(c.ENDFUNCTION(), TokenType.KEYWORD)

            val tk = c.takes()
            hub.add(tk.TAKES(), TokenType.KEYWORD)
            hub.add(tk.NOTHING(), TokenType.TYPE)

            val rt = c.returnsRule()
            hub.add(rt.RETURNS(), TokenType.KEYWORD)
            hub.add(rt.NOTHING(), TokenType.TYPE)
            hub.add(rt.ID(), TokenType.TYPE)

            hub.add(c.ID(), TokenType.FUNCTION, TokenModifier.DECLARATION)

            for (p in f.param) {
                val c = p.ctx
                when (c) {
                    is ParamContext -> {
                        hub.add(c.typename().ID(), TokenType.TYPE)
                        hub.add(c.varname().ID(), TokenType.PARAMETER, TokenModifier.DECLARATION)
                    }

                    is VariableContext -> {
                        hub.add(c.LOCAL(), TokenType.KEYWORD)
                        hub.add(c.ARRAY(), TokenType.KEYWORD)
                        hub.add(c.typename().ID(), TokenType.TYPE)
                        hub.add(c.varname().ID(), TokenType.PARAMETER, TokenModifier.DECLARATION)
                    }
                }
            }

            semanticStmt(f.stmt, hub)
        }

        return CompletableFuture.completedFuture(SemanticTokens(hub.data()))
    }

    override fun completion(params: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
        val completionItems = listOf(
            CompletionItem("Anal5"),
            CompletionItem("Cunt4")
        )
        server.log("completion")
        return CompletableFuture.completedFuture(Either.forLeft(completionItems))
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover> {
        val hover = Hover(listOf(Either.forLeft("Anal22")), null)
        return CompletableFuture.completedFuture(hover)
    }

    private fun stateUpdate(uri: String, text: String) {
        states.getOrPut(uri) { JassState() }.parse(CharStreams.fromString(text))
    }

    override fun didOpen(params: DidOpenTextDocumentParams) {
        stateUpdate(params.textDocument.uri, params.textDocument.text)
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        if (params.contentChanges.isEmpty()) return
        val uri = params.textDocument.uri
        val e = params.contentChanges.last()
        stateUpdate(uri, e.text)
    }

    override fun didSave(params: DidSaveTextDocumentParams) {
    }

    override fun resolveCompletionItem(unresolved: CompletionItem?): CompletableFuture<CompletionItem?>? {
        return CompletableFuture.completedFuture(unresolved)
    }
}


