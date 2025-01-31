package raft.war.jass.lsp.service.document.provider

import io.github.warraft.jass.antlr.JassState
import org.eclipse.lsp4j.FoldingRange
import org.eclipse.lsp4j.FoldingRangeKind

class JassFoldingRangeProvider {
    fun ranges(state: JassState?): List<FoldingRange> {
        val ranges = mutableListOf<FoldingRange>()
        if (state == null) return ranges

        for (f in state.functions) {
            if (f.tkeywords.isEmpty()) continue
            ranges.add(
                FoldingRange(
                    f.tkeywords.first().line - 1,
                    f.tkeywords.last().line - 1,
                ).apply {
                    kind = FoldingRangeKind.Region
                }
            )

        }

        return ranges
    }
}
