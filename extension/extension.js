const path = require('path');
const {LanguageClient} = require('vscode-languageclient');
const {TransportKind} = require("vscode-languageclient/node");
// noinspection NpmUsedModulesInstalled
const {workspace, Uri} = require("vscode");

/** @type {LanguageClient} */ let client;

// https://code.visualstudio.com/api/working-with-extensions/publishing-extension

// https://code.visualstudio.com/api/language-extensions/language-server-extension-guide

// noinspection JSUnusedGlobalSymbols
module.exports = {
    /** @param {ExtensionContext} context */
    activate(context) {
        const executable = {
            command: 'java',
            args: ['-jar', path.join(__dirname, '..', 'build', 'libs', 'jass-antlr-lsp.jar')],
            options: {
                env: process.env,
            },
            transport: TransportKind.stdio,
        }

        const serverOptions = {
            run: executable,
            debug: executable,
        }

        const clientOptions = {
            documentSelector: [{
                scheme: 'file',
                language: 'jass',
            }],
            synchronize: {
                fileEvents: workspace.createFileSystemWatcher('**/.clientrc')
            }
        }

        client = new LanguageClient(
            'JassAntlrLsp',
            'JassAntlrLspClient',
            serverOptions,
            clientOptions
        )

        client.onNotification('window/logMessage', params => {
            console.log(`${params.message}`);
        });

        client.start()

        workspace.updateWorkspaceFolders(
            workspace.workspaceFolders?.length ?? 0,
            null,
            {
                uri: Uri.file(path.join(context.extensionPath, 'sdk')),
                name: "JASS"
            }
        );
        // const ext = extensions.getExtension('WarRaft.jass-antlr-kotlin')
    },

    deactivate() {
        if (!client) {
            return undefined;
        }
        return client.stop();
    }
}
