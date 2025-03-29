// noinspection JSUnusedGlobalSymbols

const {LanguageClient} = require('vscode-languageclient')
// noinspection NpmUsedModulesInstalled
const {workspace, window, Uri, TreeItem, EventEmitter, ConfigurationTarget, commands} = require('vscode')
const {settingsScriptList} = require('./js/settingsScriptList')
const {W3} = require('./js/variables')
const {execFile} = require('child_process')
const path = require('path')

/**
 * @typedef {import('vscode').Uri} Uri
 * @typedef {import('vscode-languageclient').LanguageClientOptions}
 */

/** @type {LanguageClient} */ let client

// https://code.visualstudio.com/api/working-with-extensions/publishing-extension

// https://code.visualstudio.com/api/language-extensions/language-server-extension-guide

module.exports = {

    /** @param {ExtensionContext} context */
    async activate(context) {
        activateTxt2(context)

        const uri = Uri.file(path.join(__dirname, 'jass-antlr-lsp.jar'))

        try {
            await workspace.fs.stat(uri)
        } catch (error) {
            window.showErrorMessage(`Access denied: ${uri.fsPath}\n\n${error.message}`)
            return
        }

        await execFile('java', ['-jar', uri.fsPath, '-ping'], (error, stdout, stderr) => {
            if (error) {
                window.showErrorMessage(`Run failed:\n${stderr || error.message}`)
                return
            }

            if (stdout.trim() !== 'pong') {
                window.showErrorMessage(`Ping failed:\n${stdout}`)
            }
        })

        for (const l of ['jass', 'vjass', 'zinc']) {
            workspace.getConfiguration('editor', {languageId: l}).update('unicodeHighlight.ambiguousCharacters', false, ConfigurationTarget.Workspace)
        }

        let scripts = await settingsScriptList(context)
        const treeViewOptionsEmmiter = new EventEmitter()
        const treeViewOptions = {
            treeDataProvider: /** @type {TreeDataProvider} */ {
                onDidChangeTreeData: treeViewOptionsEmmiter.event,
                getTreeItem(element) {
                    return element
                },
                getChildren(element) {
                    const out = []
                    if (!element) {
                        for (const script of scripts) {
                            const item = new TreeItem(script)
                            item.command = {
                                title: 'open',
                                command: 'vscode.open',
                                arguments: [script]
                            }
                            out.push(item)
                        }
                    }
                    return out
                }
            }
        }

        client = new LanguageClient(
            'JassAntlrLsp',
            'JassAntlrLspClient',
            {
                command: 'java',
                args: ['-jar', uri.fsPath, '-lsp'],
            },
            {
                progressOnInitialization: true,
                initializationOptions: {
                    settings: {
                        [W3]: {
                            scripts: scripts.map(script => script.fsPath)
                        },
                    }
                },
                documentSelector: [
                    {
                        scheme: 'file',
                        language: 'angelscript',
                    },
                    {
                        scheme: 'file',
                        language: 'jass',
                    },
                    {
                        scheme: 'file',
                        language: 'vjass',
                    },
                    {
                        scheme: 'file',
                        language: 'zinc',
                    },
                    {
                        scheme: 'file',
                        language: 'wts',
                    },
                    {
                        scheme: 'file',
                        language: 'bni',
                    }
                ],
                middleware: {
                    workspace: {
                        /**
                         * @param {string[]} params
                         * @param {(a:string[]) => void} next
                         */
                        didChangeConfiguration: async (params, next) => {
                            if (params.indexOf(W3) >= 0) {
                                scripts = await settingsScriptList(context)
                                treeViewOptionsEmmiter.fire(null)
                            }
                            return next(params)
                        }
                    }
                },
                synchronize: {
                    configurationSection: [W3],
                    //fileEvents: workspace.createFileSystemWatcher('**/.j')
                }
            }
        )

        client.onNotification('window/logMessage', params => {
            console.log(`${params.message}`)
        })

        await client.start()


        await window.withProgress({
            location: 10, //location: vscode.ProgressLocation.Window,
            title: 'Scan Files...',
            cancellable: false,
        }, async progress => {
            const files = await workspace.findFiles('**/*.{j,vj,zn,wts}', '**/{node_modules,.git}/**')
            let count = 0
            for (const file of files) {
                count++
                progress.report({message: `[${count}/${files.length}] ${file.fsPath}`})
                try {
                    await workspace.openTextDocument(file)
                } catch (_) { /* empty */
                    window.showErrorMessage(file.fsPath)
                }
            }
        })

        window.createTreeView(W3, treeViewOptions)
    },

    async deactivate() {
        if (client) return
        await client.stop()
    }
}

// https://code.visualstudio.com/api/references/contribution-points#contributes.configuration


function activateTxt2(context) {
    let disposable = commands.registerCommand('txt2.showPreview', function () {
        const editor = window.activeTextEditor
        if (!editor || editor.document.languageId !== 'txt2') {
            window.showWarningMessage('Откройте файл .txt2 для предпросмотра')
            return
        }

        const panel = window.createWebviewPanel(
            'txt2Preview',
            'TXT2 Preview',
            //ViewColumn.Beside,
            -2,
            {enableScripts: true}
        )

        function updateWebview() {
            const text = editor.document.getText()
            panel.webview.postMessage({type: 'update', content: text})
        }

        panel.webview.html = getWebviewContent()
        updateWebview()

        workspace.onDidChangeTextDocument((event) => {
            if (event.document === editor.document) updateWebview()
        })

        panel.webview.onDidReceiveMessage((message) => {
            if (message.type === 'getText') {
                updateWebview()
            }
        })
    })

    context.subscriptions.push(disposable)
}

function getWebviewContent() {
    return `<!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <style>
        body { font-family: sans-serif; padding: 10px; }
        b { color: #ff9900; }
        i { color: #6666ff; }
        .comment { color: #888; font-style: italic; }
      </style>
    </head>
    <body>
      <div id="preview"></div>
      <script>
        const vscode = acquireVsCodeApi();

        window.addEventListener('message', (event) => {
          if (event.data.type === 'update') {
            document.getElementById('preview').innerHTML = parseTxt2(event.data.content);
          }
        });

        vscode.postMessage({ type: 'getText' });

        function parseTxt2(text) {
          text = text.replace(/\\*(.*?)\\*/g, '<b>$1</b>');  // *жирный*
          text = text.replace(/_(.*?)_/g, '<i>$1</i>');      // _курсив_
          //text = text.replace(/\\(.*)/g, '<span class="comment">//$1</span>'); // // комментарий
          return text.replace(/\\n/g, '<br>');  // Перенос строк
        }
      </script>
    </body>
    </html>`
}
