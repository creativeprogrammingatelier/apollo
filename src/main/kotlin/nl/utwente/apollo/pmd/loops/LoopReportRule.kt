package nl.utwente.apollo.pmd.loops

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.apollo.Metrics
import nl.utwente.apollo.weightedAverage

class LoopReportRule : AbstractJavaRule() {
    companion object {
        fun calculateFinal(metrics: Map<Metrics, Double>) : Double {
            return weightedAverage(
                    Pair(1.0, metrics[Metrics.LOOP_VARIETY]),
                    Pair(1.0, metrics[Metrics.LOOP_SITUATION_VARIETY])
            )
        }
    }

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        val loopVarietyMetric = LoopMetrics.LoopVarietyMetric()
        val loopVariety = loopVarietyMetric.computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.LOOP_RAW_TYPE_COUNT, loopVariety))
        val loopVarietyProb = loopVarietyMetric.computeProbability(loopVariety)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.LOOP_VARIETY, loopVarietyProb))

        val loopSituationVarietyMetric = LoopMetrics.LoopSituationVarietyMetric()
        val situationVariety = loopSituationVarietyMetric.computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.LOOP_RAW_SITUATION_COUNT, situationVariety))
        val situationVarietyProb = loopSituationVarietyMetric.computeProbability(situationVariety)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.LOOP_SITUATION_VARIETY, situationVarietyProb))

        val assessment = LoopMetrics.LoopAssessmentMetric().computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.LOOP_ASSESSMENT, assessment))

        return super.visit(node, data)
    }
}