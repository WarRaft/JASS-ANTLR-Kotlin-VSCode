const path = require('path');
const {LanguageClient, TransportKind} = require('vscode-languageclient/node');

module.exports = {
    activate(context) {
        const p = path.join(__dirname, '..', 'build', 'libs', 'ANTLR-LSP.jar')

        const executable = {
            command: 'java',
            args: ['-jar', p],
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

        context.subscriptions.push(client.start())
    },
}
