package nl.utwente.apollo.pmd.physics

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.metrics.AbstractJavaMetric
import net.sourceforge.pmd.lang.metrics.MetricOptions
import nl.utwente.apollo.s

class PhysicsMetrics {
    companion object {
        fun getResults(node: ASTCompilationUnit): Int {
            return node.jjtAccept(PVectorVisitor(), null) as Int
        }
    }

    class PVectorOperationsMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            return getResults(node).toDouble()
        }

        fun computeProbability(operationCount: Double): Double {
            return s(0.3, 3.0, operationCount)
        }
    }
}