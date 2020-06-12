package nl.utwente.apollo.pmd.oo

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.apollo.Metrics
import nl.utwente.apollo.weightedAverage

class OoReportRule : AbstractJavaRule() {
    companion object {
        fun calculateFinal(metrics: Map<Metrics, Double>) : Double {
            return weightedAverage(
                    Pair(1.0, metrics[Metrics.OO_CLASS_COUNT]),
                    Pair(1.0, metrics[Metrics.OO_SMELLS])
            )
        }
    }

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        val metric = OoMetrics.ClassCountMetric()
        val classCount = metric.computeFor(node, null)
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.OO_RAW_CLASS_COUNT, classCount))
        this.addViolationWithMessage(data, node, message, arrayOf(Metrics.OO_CLASS_COUNT, metric.computeProbability(classCount)))

        return super.visit(node, data)
    }
}