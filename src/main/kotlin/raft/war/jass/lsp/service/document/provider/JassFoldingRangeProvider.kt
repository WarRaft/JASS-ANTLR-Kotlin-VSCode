package raft.war.jass.lsp.service.document.provider

import io.github.warraft.jass.antlr.JassParser.FunctionContext
import io.github.warraft.jass.antlr.JassState
import org.eclipse.lsp4j.FoldingRange
import org.eclipse.lsp4j.FoldingRangeKind

class JassFoldingRangeProvider {
    fun ranges(state: JassState?): List<FoldingRange> {
        val ranges = mutableListOf<FoldingRange>()
        if (state == null) return ranges

        for (f in state.functions) {
            val ctx = f.ctx

            if (ctx is FunctionContext) {
                ranges.add(
                    FoldingRange(
                        ctx.FUNCTION().symbol.line - 1,
                        ctx.ENDFUNCTION().symbol.line - 1,
                    ).apply {
                        kind = FoldingRangeKind.Region
                    }
                )
            }
        }

        return ranges
    }
}
