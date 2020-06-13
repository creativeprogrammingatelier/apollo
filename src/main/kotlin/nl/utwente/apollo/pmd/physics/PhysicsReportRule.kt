package nl.utwente.apollo.pmd.physics

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.apollo.Metrics
import nl.utwente.apollo.weightedAverage

class PhysicsReportRule : AbstractJavaRule() {
    companion object {
        fun calculateFinal(metrics: Map<Metrics, Double>): Double {
            return weightedAverage(
                    Pair(3.0, metrics[Metrics.PHYSICS_RECOGNIZED_PLANS]),
                    Pair(1.0, metrics[Metrics.PHYSICS_PVECTOR_OPS])
            )
        }
    }

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        // TODO: Plans

        val opsMetric = PhysicsMetrics.PVectorOperationsMetric()
        val opsCount = opsMetric.computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.PHYSICS_RAW_PVECTOR_OP_COUNT, opsCount))
        val opsProb = opsMetric.computeProbability(opsCount)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.PHYSICS_PVECTOR_OPS, opsProb))

        return super.visit(node, data)
    }
}