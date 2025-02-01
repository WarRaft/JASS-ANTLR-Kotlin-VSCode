package raft.war.jass.lsp.service.document.provider

import io.github.warraft.jass.antlr.JassState
import org.eclipse.lsp4j.FoldingRange
import org.eclipse.lsp4j.FoldingRangeKind

class JassFoldingRangeProvider {
    fun ranges(state: JassState?): List<FoldingRange> {
        val ranges = mutableListOf<FoldingRange>()
        if (state == null) return ranges

        for (f in state.functions) {
            if (f.token.keywords.isEmpty()) continue
            ranges.add(
                FoldingRange(
                    f.token.keywords.first().line - 1,
                    f.token.keywords.last().line - 1,
                ).apply {
                    kind = FoldingRangeKind.Region
                }
            )

        }

        return ranges
    }
}
