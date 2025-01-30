package raft.war.jass.lsp

import io.github.warraft.jass.antlr.JassParser.*
import io.github.warraft.jass.antlr.JassState
import io.github.warraft.jass.antlr.psi.IJassNode
import io.github.warraft.jass.antlr.psi.JassExitWhen
import io.github.warraft.jass.antlr.psi.JassFun
import io.github.warraft.jass.antlr.psi.JassIf
import io.github.warraft.jass.antlr.psi.JassLoop
import io.github.warraft.jass.antlr.psi.JassReturn
import io.github.warraft.jass.antlr.psi.JassVar
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

        if (position == null) {
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
        for (node in nodes) {
            when (node) {
                is JassVar -> {
                    val ctx = node.ctx
                    if (ctx is StmtSetContext) {
                        hub.add(ctx.set().SET(), TokenType.KEYWORD)
                        hub.add(ctx.set().ID(), TokenType.VARIABLE)
                    }
                }

                is JassFun -> {
                    val ctx = node.ctx
                    if (ctx is CallContext) {
                        hub.add(ctx.CALL(), TokenType.KEYWORD)
                        hub.add(ctx.DEBUG(), TokenType.KEYWORD)
                        hub.add(ctx.ID(), TokenType.FUNCTION)
                    }
                }

                is JassIf -> {
                    var ctx = node.ctx
                    if (ctx is IfRuleContext) {
                        hub.add(ctx.IF(), TokenType.KEYWORD)
                        hub.add(ctx.THEN(), TokenType.KEYWORD)
                        hub.add(ctx.ENDIF(), TokenType.KEYWORD)
                    }
                    semanticStmt(node.stmt, hub)

                    for (elseif in node.elseifs) {
                        val ctx = elseif.ctx
                        if (ctx is ElseifContext) {
                            hub.add(ctx.ELSEIF(), TokenType.KEYWORD)
                            hub.add(ctx.THEN(), TokenType.KEYWORD)
                        }
                        semanticStmt(elseif.stmt, hub)
                    }

                    val elser = node.elser
                    val elsectx = elser?.ctx
                    if (elsectx is ElseRuleContext) {
                        hub.add(elsectx.ELSE(), TokenType.KEYWORD)
                        semanticStmt(elser.stmt, hub)
                    }
                }

                is JassLoop -> {
                    var ctx = node.ctx
                    if (ctx is LoopContext) {
                        hub.add(ctx.LOOP(), TokenType.KEYWORD)
                        hub.add(ctx.ENDLOOP(), TokenType.KEYWORD)
                        semanticStmt(node.stmt, hub)
                    }
                }

                is JassExitWhen -> {
                    var ctx = node.ctx
                    if (ctx is ExitwhenContext) {
                        hub.add(ctx.EXITWHEN(), TokenType.KEYWORD)
                    }
                }

                is JassReturn -> {
                    var ctx = node.ctx
                    if (ctx is ReturnRuleContext) {
                        hub.add(ctx.RETURN(), TokenType.KEYWORD)
                    }
                }
            }
        }
    }

    override fun semanticTokensFull(params: SemanticTokensParams?): CompletableFuture<SemanticTokens> {
        val state = states[params?.textDocument?.uri]
        if (state == null) return CompletableFuture.completedFuture(SemanticTokens())

        val hub = TokenHub()

        for (ctx in state.globalsCtx) {
            hub.add(ctx.GLOBALS(), TokenType.KEYWORD)
            hub.add(ctx.ENDGLOBALS(), TokenType.KEYWORD)
        }

        for (g in state.globals) {
            val ctx = g.ctx
            if (ctx is VariableContext) {
                hub.add(ctx.CONSTANT(), TokenType.KEYWORD)
                hub.add(ctx.ARRAY(), TokenType.KEYWORD)
                hub.add(ctx.varname().ID(), TokenType.VARIABLE, TokenModifier.DECLARATION)
                hub.add(ctx.typename().ID(), TokenType.TYPE)
            }
        }

        for (f in state.functions) {
            val ctx = f.ctx

            if (ctx is FunctionContext) {
                hub.add(ctx.FUNCTION(), TokenType.KEYWORD)
                hub.add(ctx.ENDFUNCTION(), TokenType.KEYWORD)

                val tk = ctx.takes()
                hub.add(tk.TAKES(), TokenType.KEYWORD)
                hub.add(tk.NOTHING(), TokenType.TYPE)

                val rt = ctx.returnsRule()
                hub.add(rt.RETURNS(), TokenType.KEYWORD)
                hub.add(rt.NOTHING(), TokenType.TYPE)
                hub.add(rt.ID(), TokenType.TYPE)

                hub.add(ctx.ID(), TokenType.FUNCTION, TokenModifier.DECLARATION)
            }

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

    override fun diagnostic(params: DocumentDiagnosticParams?): CompletableFuture<DocumentDiagnosticReport?> {
        val uri = params?.textDocument?.uri ?: return CompletableFuture.completedFuture(
            DocumentDiagnosticReport(RelatedUnchangedDocumentDiagnosticReport())
        )
        val diagnostics = listOf<Diagnostic>(
            /*
            Diagnostic(
                Range(Position(0, 0), Position(0, 8)),
                "Error: Testing diagnostic",
                DiagnosticSeverity.Error,
                "JASS"
            )
             */
        )

        val report = RelatedFullDocumentDiagnosticReport(diagnostics)
        return CompletableFuture.completedFuture(DocumentDiagnosticReport(report))
    }
}


