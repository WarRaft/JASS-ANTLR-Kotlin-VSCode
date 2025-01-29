import org.eclipse.lsp4j.launch.LSPLauncher.createServerLauncher
import raft.war.jass.lsp.JassLanguageServer

fun main() {
    val server = JassLanguageServer()
    val launcher = createServerLauncher(server, System.`in`, System.out)
    server.connect(launcher.remoteProxy)
    launcher.startListening().get()
}
