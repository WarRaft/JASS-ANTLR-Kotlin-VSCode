package raft.war.jass.lsp.token

import org.antlr.v4.runtime.tree.TerminalNode

class TokenHub {
    val lines = mutableMapOf<Int, TokenLine>()
    fun add(node: TerminalNode?, type: TokenType, modifier: TokenModifier? = null) {
        if (node == null) return
        val s = node.symbol
        val t = Token(
            line = s.line - 1,
            pos = s.charPositionInLine,
            len = s.text.length,
            type = type,
            modifier = modifier
        )
        lines.getOrPut(t.line) { TokenLine(index = t.line) }.add(t)
    }

    fun data(): MutableList<Int> {
        val l = mutableListOf<Int>()
        var lineLast = 0

        val lines = this.lines.values.sortedBy { it.index }
        for (line in lines) {
            val tokens = line.tokens.sortedBy { it.pos }
            var tokenLast = 0
            for ((index, token) in tokens.withIndex()) {
                l.add(if (index == 0) token.line - lineLast else 0)
                l.add(token.pos - tokenLast)
                l.add(token.len)
                l.add(token.type.ordinal)
                l.add((token.modifier?.ordinal ?: -1) + 1)
                tokenLast = token.pos
            }
            lineLast = line.index
        }

        return l
    }
}
