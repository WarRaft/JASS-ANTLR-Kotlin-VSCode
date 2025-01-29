package raft.war.jass.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.*
import java.util.concurrent.CompletableFuture


internal class ExampleLanguageServer : LanguageServer, LanguageClientAware {
    private var client: LanguageClient? = null

    @Suppress("unused")
    private var workspaceRoot: String? = null
    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult?> {
        val capabilities = ServerCapabilities()
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full)
        capabilities.setCodeActionProvider(false)
        capabilities.completionProvider = CompletionOptions(true, null)

        return CompletableFuture.completedFuture<InitializeResult?>(InitializeResult(capabilities))
    }

    override fun shutdown(): CompletableFuture<Any> = CompletableFuture.completedFuture(null)
    override fun exit() = Unit

    private val fullTextDocumentService: FullTextDocumentService = object : FullTextDocumentService() {
        public override fun completion(textDocumentPosition: TextDocumentPositionParams?): CompletableFuture<CompletionList?> {
            val typescriptCompletionItem = CompletionItem()
            typescriptCompletionItem.label = "TypeScript"
            typescriptCompletionItem.kind = CompletionItemKind.Text
            typescriptCompletionItem.data = 1.0

            val javascriptCompletionItem = CompletionItem()
            javascriptCompletionItem.label = "JavaScript"
            javascriptCompletionItem.kind = CompletionItemKind.Text
            javascriptCompletionItem.data = 2.0

            val completions: MutableList<CompletionItem?> = ArrayList<CompletionItem?>()
            completions.add(typescriptCompletionItem)
            completions.add(javascriptCompletionItem)

            return CompletableFuture.completedFuture<CompletionList?>(CompletionList(false, completions))
        }

        /*
        fun resolveCompletionItem(item: CompletionItem): CompletableFuture<CompletionItem?> {
            if (item.data.equals(1.0)) {
                item.detail = "TypeScript details"
                item.setDocumentation("TypeScript documentation")
            } else if (item.data.equals(2.0)) {
                item.detail = "JavaScript details"
                item.setDocumentation("JavaScript documentation")
            }
            return CompletableFuture.completedFuture<CompletionItem?>(item)
        }

         */

        public override fun didChange(params: DidChangeTextDocumentParams) {
            super.didChange(params)

            val document: TextDocumentItem? = this.documents.get(params.textDocument.uri)
            validateDocument(document)
        }
    }

    override fun getTextDocumentService(): TextDocumentService {
        return fullTextDocumentService
    }

    private fun validateDocument(document: TextDocumentItem?) {
        val diagnostics: MutableList<Diagnostic?> = ArrayList<Diagnostic?>()
        val lines = document?.text?.split("\\r?\\n".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
        var problems = 0
        var i = 0
        while (i < lines!!.size && problems < maxNumberOfProblems) {
            val line = lines[i]
            val index = line.indexOf("typescript")
            if (index >= 0) {
                problems++
                val diagnostic = Diagnostic()
                diagnostic.severity = DiagnosticSeverity.Warning
                diagnostic.range = Range(Position(i, index), Position(i, index + 10))
                diagnostic.message = String.format(
                    "%s should be spelled TypeScript",
                    line.substring(index, index + 10)
                )
                diagnostic.source = "ex"
                diagnostics.add(diagnostic)
            }
            i++
        }

        client!!.publishDiagnostics(PublishDiagnosticsParams(document.getUri(), diagnostics))
    }

    private var maxNumberOfProblems = 100

    override fun getWorkspaceService(): WorkspaceService {
        return object : WorkspaceService {
            override fun symbol(params: WorkspaceSymbolParams?): CompletableFuture<Either<List<SymbolInformation?>?, List<WorkspaceSymbol?>?>?>? {
                return null
            }

            override fun didChangeConfiguration(params: DidChangeConfigurationParams) {
                val settings = params.settings as MutableMap<*, *>
                val languageServerExample = settings["languageServerExample"] as MutableMap<*, *>
                maxNumberOfProblems =
                    (languageServerExample.getOrDefault("maxNumberOfProblems", 100.0) as Double).toInt()
                fullTextDocumentService.documents.values.forEach({ d -> validateDocument(d) })
            }

            override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
                client!!.logMessage(MessageParams(MessageType.Log, "We received an file change event"))
            }
        }
    }

    override fun connect(client: LanguageClient?) {
        this.client = client
    }
}
