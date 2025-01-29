const path = require('path');
const {LanguageClient} = require('vscode-languageclient');
const {TransportKind} = require("vscode-languageclient/node");

// noinspection JSUnusedGlobalSymbols
module.exports = {
    activate(context) {
        const executable = {
            command: 'java',
            args: ['-jar', path.join(__dirname, '..', 'build', 'libs', 'ANTLR-LSP.jar')],
            options: {
                env: process.env,
            },
            transportKind: TransportKind.stdio,
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
        }

        const client = new LanguageClient(
            'AntlrJassLsp',
            'AntlrJassLspClient',
            serverOptions,
            clientOptions
        )

        client.trace = 2; // Trace.Verbose

        context.subscriptions.push(client.start())

        client.onNotification('window/logMessage', params => {
            const messageType = ['Error', 'Warning', 'Info', 'Log'][params.type - 1];
            console.log(`[${messageType}] ${params.message}`);
        });
    },
}
