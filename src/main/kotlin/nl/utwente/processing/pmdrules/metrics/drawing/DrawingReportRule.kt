package nl.utwente.processing.pmdrules.metrics.drawing

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.processing.pmdrules.metrics.Metrics
import nl.utwente.processing.pmdrules.metrics.weightedAverage

class DrawingReportRule : AbstractJavaRule() {
    companion object {
        fun calculateFinal(metrics: Map<Metrics, Double>) : Double {
            return weightedAverage(
                    Pair(3.0, metrics[Metrics.DRAWING_USED]),
                    Pair(2.0, metrics[Metrics.DRAWING_ADVANCED]),
                    Pair(1.0, metrics[Metrics.DRAWING_UNCOVERED])
            )
        }
    }

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        val usedDrawingsMetric = DrawingMetrics.UsedDrawingsMetric()
        val usedDrawings = usedDrawingsMetric.computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.DRAWING_RAW_COVERED_COUNT, usedDrawings))
        val usedDrawingsProb = usedDrawingsMetric.computeProbability(usedDrawings)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.DRAWING_USED, usedDrawingsProb))

        val advancedDrawingsMetric = DrawingMetrics.AdvancedDrawingsMetric()
        val advancedDrawings = advancedDrawingsMetric.computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.DRAWING_RAW_ADVANCED_COUNT, advancedDrawings))
        val advancedDrawingsProb = advancedDrawingsMetric.computeProbability(advancedDrawings)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.DRAWING_ADVANCED, advancedDrawingsProb))

        val uncoveredDrawingsMetric = DrawingMetrics.UncoveredDrawingsMetric()
        val uncoveredDrawings = uncoveredDrawingsMetric.computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.DRAWING_RAW_UNCOVERED_COUNT, uncoveredDrawings))
        val uncoveredDrawingsProb = uncoveredDrawingsMetric.computeProbability(uncoveredDrawings)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.DRAWING_UNCOVERED, uncoveredDrawingsProb))

        return super.visit(node, data)
    }
}
