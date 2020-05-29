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
        val usedDrawings = DrawingMetrics.UsedDrawingsMetric().computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.DRAWING_USED, usedDrawings))

        val advancedDrawings = DrawingMetrics.AdvancedDrawingsMetric().computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.DRAWING_ADVANCED, advancedDrawings))

        val uncoveredDrawings = DrawingMetrics.UncoveredDrawingsMetric().computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.DRAWING_UNCOVERED, uncoveredDrawings))

        return super.visit(node, data)
    }
}
