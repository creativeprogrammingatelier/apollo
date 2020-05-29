package nl.utwente.processing.pmdrules.metrics.loops

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.processing.pmdrules.metrics.Metrics
import nl.utwente.processing.pmdrules.metrics.weightedAverage

class LoopReportRule : AbstractJavaRule() {
    companion object {
        fun calculateFinal(metrics: Map<Metrics, Double>) : Double {
            return weightedAverage(
                    Pair(1.0, metrics[Metrics.LOOP_VARIETY]),
                    Pair(1.0, metrics[Metrics.LOOP_SITUATIONVARIETY])
            )
        }
    }

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        val loopVariety = LoopMetrics.LoopVarietyMetric().computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.LOOP_VARIETY, loopVariety))

        val situationVarietyMetric = LoopMetrics.LoopSituationVarietyMetric().computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.LOOP_SITUATIONVARIETY, situationVarietyMetric))

        return super.visit(node, data)
    }
}