package nl.utwente.apollo.pmd.physics

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.metrics.AbstractJavaMetric
import net.sourceforge.pmd.lang.metrics.MetricOptions
import nl.utwente.apollo.s

class PhysicsMetrics {
    class PlansMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            node.jjtAccept(PhysicsDataFlowVisitor(), null)
            return 0.0
        }

        fun computeProbability(operationCount: Double): Double {
            return s(0.43, 3.03, operationCount)
        }
    }

    class PVectorOperationsMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            return (node.jjtAccept(PVectorVisitor(), null) as Int).toDouble()
        }

        fun computeProbability(operationCount: Double): Double {
            return s(3.59, 1.74, operationCount)
        }
    }
}