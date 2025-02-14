// noinspection JSUnusedGlobalSymbols

const {LanguageClient} = require('vscode-languageclient')
// noinspection NpmUsedModulesInstalled
const {workspace, window, Uri, EventEmitter, commands} = require('vscode')

/**
 * @typedef {import('vscode').Uri} Uri
 * @typedef {import('vscode-languageclient').LanguageClientOptions}
 */

/** @type {LanguageClient} */ let client

// https://code.visualstudio.com/api/working-with-extensions/publishing-extension

// https://code.visualstudio.com/api/language-extensions/language-server-extension-guide

const W3 = 'Warcraft'

class FileNode {
    constructor(name, uri) {
        this.name = name
        this.uri = uri
        this.label = name
        this.collapsibleState = 0 // TreeItemCollapsibleState.None
    }
}

class FileExplorerProvider {
    constructor() {
        this._onDidChangeTreeData = new EventEmitter()
        this.onDidChangeTreeData = this._onDidChangeTreeData.event
    }

    getTreeItem(element) {
        return element
    }

    getChildren(element) {
        if (!element) {
            return [
                new FileNode('file1.txt', '/абсолютный/путь/к/file1.txt'),
                new FileNode('file2.log', '/другой/путь/file2.log')
            ]
        }
        return []
    }

    refresh() {
        this._onDidChangeTreeData.fire('')
    }
}

/**
 * @param {ExtensionContext} context
 * @return {Promise<Uri[]>}
 */
const scriptList = async context => {
    /** @type {Uri[]} */ const uris = []

    /** @type {string[]} */  const scripts = workspace.getConfiguration(W3).scripts
    scripts.push('/Users/nazarpunk/IdeaProjects/JASS-ANTLR-Kotlin-VSCode/sdk/common.j')

    for (let script of scripts) {
        script = script.trimStart()

        let uri = context.extensionUri
        if (script.startsWith('{extension}')) {
            script = script.replace('{extension}', '').trim()
            uri = Uri.joinPath(uri, 'sdk', script)
        } else {
            uri = Uri.parse(script)
        }

        /** @type {FileStat} */ let stat
        try {
            stat = await workspace.fs.stat(uri)
        } catch (e) {
            window.showErrorMessage(`No such file or directory: ${uri.fsPath}`)
            continue
        }

        switch (stat.type) {
            case 0:
                window.showErrorMessage(`Unknown file type: ${uri.fsPath}`)
                continue
            case 1:
                uris.push(uri)
                break
            case 2:
                window.showErrorMessage(`Directory not supported: ${uri.fsPath}`)
                continue
            case 64:
                window.showErrorMessage(`SymbolicLink not supported: ${uri.fsPath}`)
        }
    }

    scripts.length = 0
    for (const uri of uris) scripts.push(uri.fsPath)

    return uris
}


module.exports = {
    /** @param {ExtensionContext} context */
    async activate(context) {
        const scripts = await scriptList(context)

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
                                await scriptList(context)
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

        const fileExplorerProvider = new FileExplorerProvider()

        window.createTreeView('WarCraft', {
            treeDataProvider: fileExplorerProvider
        })

        commands.registerCommand('WarCraft.openFile', uri => {
            window.showTextDocument(Uri.file(uri))
        })

        commands.registerCommand('WarCraft.refresh', () => {
            fileExplorerProvider.refresh()
        })

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
    },

    async deactivate() {
        if (client) return
        await client.stop()
    }
}

// https://code.visualstudio.com/api/references/contribution-points#contributes.configuration
