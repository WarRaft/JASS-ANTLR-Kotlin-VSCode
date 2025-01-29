package raft.war.jass.lsp.token

class Token(val line: Int, val pos: Int, val len: Int, val type: TokenType, val modifier: TokenModifier? = null)
