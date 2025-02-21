// noinspection JSUnusedGlobalSymbols

const {LanguageClient} = require('vscode-languageclient')
// noinspection NpmUsedModulesInstalled
const {workspace, window, Uri, TreeItem, EventEmitter} = require('vscode')
const {settingsScriptList} = require('./js/settingsScriptList')
const {W3} = require('./js/variables')

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
                args: ['-jar', Uri.joinPath(Uri.parse(__dirname), 'jass-antlr-lsp.jar').fsPath, '-lsp4j'],
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
                        language: 'jass',
                    },
                    {
                        scheme: 'file',
                        language: 'vjass',
                    },
                    {
                        scheme: 'file',
                        language: 'zinc',
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
            const files = await workspace.findFiles('**/*.{j,vj,zn}', '**/{node_modules,.git}/**')
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
