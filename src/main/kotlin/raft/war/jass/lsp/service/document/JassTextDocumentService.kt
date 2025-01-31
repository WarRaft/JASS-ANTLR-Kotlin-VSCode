package raft.war.jass.lsp.service.document

import io.github.warraft.jass.antlr.JassState
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import raft.war.jass.lsp.JassLanguageServer
import raft.war.jass.lsp.service.document.provider.JassFoldingRangeProvider
import raft.war.jass.lsp.service.document.provider.JassSemanticTokensFullProvider
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import kotlin.io.path.isReadable
import kotlin.io.path.toPath

class JassTextDocumentService(val server: JassLanguageServer) : TextDocumentService {
    private val states = mutableMapOf<Path, JassState>()

    val semanticTokensFullProvider = JassSemanticTokensFullProvider()
    override fun semanticTokensFull(params: SemanticTokensParams?): CompletableFuture<SemanticTokens> =
        CompletableFuture.completedFuture(semanticTokensFullProvider.tokens(getState(params?.textDocument?.uri)))
    
    val foldingRangeProvider = JassFoldingRangeProvider()
    override fun foldingRange(params: FoldingRangeRequestParams?): CompletableFuture<List<FoldingRange>> =
        CompletableFuture.completedFuture(foldingRangeProvider.ranges(getState(params?.textDocument?.uri)))

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

    private fun getState(uri: String?): JassState? = if (uri == null) null else states[URI(uri).toPath()]

    private fun getStateAndParse(
        path: Path,
        stream: CharStream,
        list: List<JassState> = listOf(),
        noparse: Boolean,
    ): JassState {
        val s = states.getOrPut(path) { JassState() }
        if (noparse && s.nodeMap.isNotEmpty()) return s
        s.parse(stream, list)
        return s
    }

    private fun stateUpdate(uri: String, text: String) {
        val path = URI(uri).toPath()
        val list = mutableListOf<JassState>()

        val folder = server.args.getOrNull(1)
        if (folder != null) {
            for (f in listOf("common.j", "blizzard.j")) {
                val p = Paths.get(folder, f)
                if (p == path) {
                    getStateAndParse(path, CharStreams.fromString(text), list, false)
                    return
                }
                if (!p.isReadable()) continue
                list.add(getStateAndParse(p, CharStreams.fromPath(p), list, true))
            }
        }

        getStateAndParse(path, CharStreams.fromString(text), list, false)
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
