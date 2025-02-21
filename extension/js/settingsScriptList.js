// noinspection NpmUsedModulesInstalled
const {workspace, window, Uri} = require('vscode')
const {W3} = require('./variables')
module.exports = {
    /**
     * @param {ExtensionContext} context
     * @return {Promise<Uri[]>}
     */
    async settingsScriptList(context) {
        /** @type {Uri[]} */ const uris = []

        /** @type {string[]} */  const scripts = workspace.getConfiguration(W3).scripts
        for (let script of scripts) {
            script = script.trimStart()

            let uri = context.extensionUri
            if (script.startsWith('?')) {
                script = script.replace(/^\s*\?/, '').trim()
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

}
