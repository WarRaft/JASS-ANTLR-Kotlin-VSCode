package raft.war.jass.lsp.token

class TokenLine(val index: Int) {
    val tokens = mutableListOf<Token>()

    fun add(token: Token) {
        tokens.add(token)
    }
}
