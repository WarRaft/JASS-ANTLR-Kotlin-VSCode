import org.eclipse.lsp4j.launch.LSPLauncher.createServerLauncher
import raft.war.jass.lsp.JassLanguageServer

fun main() {
    try {
        val input = System.`in`
        val output = System.out
        val server = JassLanguageServer()
        val launcher = createServerLauncher(server, input, output)
        launcher.startListening()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
