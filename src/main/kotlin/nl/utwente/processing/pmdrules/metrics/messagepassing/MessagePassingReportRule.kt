package nl.utwente.processing.pmdrules.metrics.messagepassing

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.processing.pmdrules.metrics.Metrics
import nl.utwente.processing.pmdrules.metrics.weightedAverage

class MessagePassingReportRule : AbstractJavaRule() {
    companion object {
        fun calculateFinal(metrics: Map<Metrics, Double>) : Double {
            return weightedAverage(
                    Pair(1.0, metrics[Metrics.MESSAGEPASSING_RATIO])
            )
        }
    }

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        val ratio = MessagePassingMetrics.RatioMetric().computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.MESSAGEPASSING_RATIO, ratio))

        return super.visit(node, data)
    }
}