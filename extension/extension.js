const path = require('path');
const {LanguageClient} = require('vscode-languageclient');
// noinspection NpmUsedModulesInstalled
const {workspace, Uri} = require("vscode");

/** @type {LanguageClient} */ let client;

// https://code.visualstudio.com/api/working-with-extensions/publishing-extension

// https://code.visualstudio.com/api/language-extensions/language-server-extension-guide

// noinspection JSUnusedGlobalSymbols
module.exports = {
    /** @param {ExtensionContext} context */
    activate(context) {
        const sdk = path.join(context.extensionPath, 'sdk')

        const executable = {
            command: 'java',
            args: [
                '-jar', path.join(__dirname, '..', 'build', 'libs', 'jass-antlr-lsp.jar'),
                '-sdk', sdk
            ],
            options: {
                env: process.env,
            }
        }

        client = new LanguageClient(
            'JassAntlrLsp',
            'JassAntlrLspClient',
            {
                run: executable,
                debug: executable,
            },
            {
                documentSelector: [{
                    scheme: 'file',
                    language: 'jass',
                }],
                synchronize: {
                    fileEvents: workspace.createFileSystemWatcher('**/.clientrc')
                }
            }
        )

        client.onNotification('window/logMessage', params => {
            console.log(`${params.message}`);
        });

        client.start()

        if (!workspace.workspaceFolders?.some(folder => folder.uri.fsPath === sdk)) {
            workspace.updateWorkspaceFolders(
                workspace.workspaceFolders?.length ?? 0,
                null,
                {
                    uri: Uri.file(sdk),
                    name: "JASS"
                }
            );
        }
    },

    deactivate() {
        if (!client) {
            return undefined;
        }
        return client.stop();
    }
}
