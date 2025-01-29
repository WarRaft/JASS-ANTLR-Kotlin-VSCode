package raft.war.jass.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture

internal open class FullTextDocumentService : TextDocumentService {
    var documents: HashMap<String?, TextDocumentItem?> = HashMap<String?, TextDocumentItem?>()

    public open fun completion(position: TextDocumentPositionParams?): CompletableFuture<CompletionList?>? {
        return null
    }

    override fun resolveCompletionItem(unresolved: CompletionItem?): CompletableFuture<CompletionItem?>? {
        return null
    }

    public fun hover(position: TextDocumentPositionParams?): CompletableFuture<Hover?>? {
        return null
    }

    public fun signatureHelp(position: TextDocumentPositionParams?): CompletableFuture<SignatureHelp?>? {
        return null
    }

    public fun definition(position: TextDocumentPositionParams?): CompletableFuture<MutableList<out Location?>?>? {
        return null
    }

    override fun references(params: ReferenceParams?): CompletableFuture<MutableList<out Location?>?>? {
        return null
    }

    public fun documentHighlight(position: TextDocumentPositionParams?): CompletableFuture<MutableList<out DocumentHighlight?>?>? {
        return null
    }

    override fun documentSymbol(params: DocumentSymbolParams?): CompletableFuture<List<Either<SymbolInformation?, DocumentSymbol?>?>?>? {
        return null
    }

    override fun codeAction(params: CodeActionParams?): CompletableFuture<List<Either<Command?, CodeAction?>?>?>? {
        return null
    }

    override fun codeLens(params: CodeLensParams?): CompletableFuture<MutableList<out CodeLens?>?>? {
        return null
    }

    override fun resolveCodeLens(unresolved: CodeLens?): CompletableFuture<CodeLens?>? {
        return null
    }

    override fun formatting(params: DocumentFormattingParams?): CompletableFuture<MutableList<out TextEdit?>?>? {
        return null
    }

    override fun rangeFormatting(params: DocumentRangeFormattingParams?): CompletableFuture<MutableList<out TextEdit?>?>? {
        return null
    }

    override fun onTypeFormatting(params: DocumentOnTypeFormattingParams?): CompletableFuture<MutableList<out TextEdit?>?>? {
        return null
    }

    override fun rename(params: RenameParams?): CompletableFuture<WorkspaceEdit?>? {
        return null
    }

    override fun didOpen(params: DidOpenTextDocumentParams) {
        documents.put(params.textDocument.uri, params.textDocument)
    }

    override fun didChange(params: DidChangeTextDocumentParams) {
        val uri = params.textDocument.uri
        for (changeEvent in params.contentChanges) {
            if (changeEvent.range != null) {
                throw UnsupportedOperationException("Range should be null for full document update.")
            }
            documents.get(uri)!!.text = changeEvent.text
        }
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        val uri = params.textDocument.uri
        documents.remove(uri)
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
    }
}
