package raft.war.jass.lsp.service.document.provider

import io.github.warraft.jass.antlr.JassParser
import io.github.warraft.jass.antlr.JassState
import io.github.warraft.jass.antlr.psi.IJassNode
import io.github.warraft.jass.antlr.psi.JassExitWhen
import io.github.warraft.jass.antlr.psi.JassFun
import io.github.warraft.jass.antlr.psi.JassIf
import io.github.warraft.jass.antlr.psi.JassLoop
import io.github.warraft.jass.antlr.psi.JassReturn
import io.github.warraft.jass.antlr.psi.JassVar
import org.eclipse.lsp4j.SemanticTokens
import raft.war.jass.lsp.token.TokenHub
import raft.war.jass.lsp.token.TokenModifier
import raft.war.jass.lsp.token.TokenType

class JassSemanticTokensFullProvider {

    fun tokens(state: JassState?): SemanticTokens {
        if (state == null) return SemanticTokens()

        val hub = TokenHub()

        for (c in state.comments) {
            hub.add(c, TokenType.COMMENT)
        }

        for (t in state.types) {
            val ctx = t.ctx
            if (ctx is JassParser.TypeContext) {
                hub.add(ctx.TYPE(), TokenType.KEYWORD)
                hub.add(ctx.typename().ID(), TokenType.TYPE, TokenModifier.DECLARATION)
                val ext = ctx.extendsRule()
                hub.add(ext.EXTENDS(), TokenType.KEYWORD)
                hub.add(ext.typename().ID(), TokenType.TYPE)
            }
        }


        for (g in state.globalsCtx) {
            hub.add(g.GLOBALS(), TokenType.KEYWORD)
            hub.add(g.ENDGLOBALS(), TokenType.KEYWORD)
        }

        for (g in state.globals) {
            val ctx = g.ctx
            if (ctx is JassParser.VariableContext) {
                hub.add(ctx.CONSTANT(), TokenType.KEYWORD)
                hub.add(ctx.ARRAY(), TokenType.KEYWORD)
                hub.add(ctx.varname().ID(), TokenType.VARIABLE, TokenModifier.DECLARATION)
                hub.add(ctx.typename().ID(), TokenType.TYPE)
            }
        }

        for (f in state.natives) {
            function(f, hub)
        }

        for (f in state.functions) {
            function(f, hub)
        }


        return SemanticTokens(hub.data())
    }

    fun param(list: MutableList<JassVar>, hub: TokenHub) {
        for (p in list) {
            val c = p.ctx
            when (c) {
                is JassParser.ParamContext -> {
                    hub.add(c.typename().ID(), TokenType.TYPE)
                    hub.add(c.varname().ID(), TokenType.PARAMETER, TokenModifier.DECLARATION)
                }

                is JassParser.VariableContext -> {
                    hub.add(c.LOCAL(), TokenType.KEYWORD)
                    hub.add(c.ARRAY(), TokenType.KEYWORD)
                    hub.add(c.typename().ID(), TokenType.TYPE)
                    hub.add(c.varname().ID(), TokenType.PARAMETER, TokenModifier.DECLARATION)
                }
            }
        }
    }

    fun function(f: JassFun, hub: TokenHub) {
        for (k in f.tkeywords) {
            hub.add(k, TokenType.KEYWORD)
        }

        hub.add(f.tname, TokenType.FUNCTION, TokenModifier.DECLARATION)
        hub.add(f.ttype, TokenType.TYPE)

        param(f.param, hub)
        stmt(f.stmt, hub)
    }

    fun stmt(nodes: List<IJassNode>, hub: TokenHub) {
        for (node in nodes) {
            when (node) {
                is JassVar -> {
                    val ctx = node.ctx
                    if (ctx is JassParser.StmtSetContext) {
                        hub.add(ctx.set().SET(), TokenType.KEYWORD)
                        hub.add(ctx.set().ID(), TokenType.VARIABLE)
                    }
                }

                is JassFun -> {
                    /*
                    val ctx = node.ctx
                    if (ctx is JassParser.CallContext) {
                        hub.add(ctx.CALL(), TokenType.KEYWORD)
                        hub.add(ctx.DEBUG(), TokenType.KEYWORD)
                        hub.add(ctx.ID(), TokenType.FUNCTION)
                    }

                     */
                }

                is JassIf -> {
                    var ctx = node.ctx
                    if (ctx is JassParser.IfRuleContext) {
                        hub.add(ctx.IF(), TokenType.KEYWORD)
                        hub.add(ctx.THEN(), TokenType.KEYWORD)
                        hub.add(ctx.ENDIF(), TokenType.KEYWORD)
                    }
                    stmt(node.stmt, hub)

                    for (elseif in node.elseifs) {
                        val ctx = elseif.ctx
                        if (ctx is JassParser.ElseifContext) {
                            hub.add(ctx.ELSEIF(), TokenType.KEYWORD)
                            hub.add(ctx.THEN(), TokenType.KEYWORD)
                        }
                        stmt(elseif.stmt, hub)
                    }

                    val elser = node.elser
                    val elsectx = elser?.ctx
                    if (elsectx is JassParser.ElseRuleContext) {
                        hub.add(elsectx.ELSE(), TokenType.KEYWORD)
                        stmt(elser.stmt, hub)
                    }
                }

                is JassLoop -> {
                    var ctx = node.ctx
                    if (ctx is JassParser.LoopContext) {
                        hub.add(ctx.LOOP(), TokenType.KEYWORD)
                        hub.add(ctx.ENDLOOP(), TokenType.KEYWORD)
                        stmt(node.stmt, hub)
                    }
                }

                is JassExitWhen -> {
                    var ctx = node.ctx
                    if (ctx is JassParser.ExitwhenContext) {
                        hub.add(ctx.EXITWHEN(), TokenType.KEYWORD)
                    }
                }

                is JassReturn -> {
                    var ctx = node.ctx
                    if (ctx is JassParser.ReturnRuleContext) {
                        hub.add(ctx.RETURN(), TokenType.KEYWORD)
                    }
                }
            }
        }
    }
}
