package raft.war.jass.lsp.token

import kotlin.test.Test

class TokenTypeTest {
    @Test
    fun list() {
        println(TokenType.KEYWORD.ordinal)
        println(TokenType.entries.map { it -> it.name.lowercase() })
    }
}
