const path = require('path');
const {LanguageClient} = require('vscode-languageclient');
const {TransportKind} = require("vscode-languageclient/node");
const {workspace} = require("vscode");

let client;

// noinspection JSUnusedGlobalSymbols
module.exports = {
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

        client.trace = 2; // Trace.Verbose

        client.onNotification('window/logMessage', params => {
            console.log(`${params.message}`);
        });

        context.subscriptions.push(client.start())
    },

    deactivate() {
        if (!client) {
            return undefined;
        }
        return client.stop();
    }
}
