package nl.utwente.apollo.pmd.physics

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.metrics.AbstractJavaMetric
import net.sourceforge.pmd.lang.metrics.MetricOptions
import nl.utwente.apollo.s

class PhysicsMetrics {
    companion object {
        fun getResults(node: ASTCompilationUnit): Pair<Int, Int> {
            return node.jjtAccept(PVectorVisitor(), null) as Pair<Int, Int>
        }

        fun getOpsCount(node: ASTCompilationUnit): Int {
            return getResults(node).first
        }

        fun getPlanMatchCount(node: ASTCompilationUnit): Int {
            return getResults(node).second
        }
    }

    class PlansMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            return getPlanMatchCount(node).toDouble()
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
            return getOpsCount(node).toDouble()
        }

        fun computeProbability(operationCount: Double): Double {
            return s(3.59, 1.74, operationCount)
        }
    }
}