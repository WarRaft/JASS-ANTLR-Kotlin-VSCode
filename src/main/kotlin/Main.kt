import org.eclipse.lsp4j.launch.LSPLauncher.createServerLauncher
import raft.war.jass.lsp.JassLanguageServer

fun main(args: Array<String>) {
    val server = JassLanguageServer(args)
    val launcher = createServerLauncher(server, System.`in`, System.out)
    server.connect(launcher.remoteProxy)
    launcher.startListening().get()
}
