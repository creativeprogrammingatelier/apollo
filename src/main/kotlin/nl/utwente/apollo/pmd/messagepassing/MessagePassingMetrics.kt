package nl.utwente.apollo.pmd.messagepassing

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.metrics.AbstractJavaMetric
import net.sourceforge.pmd.lang.metrics.MetricOptions
import org.apache.commons.lang3.mutable.MutableInt

class MessagePassingMetrics {

    class GlobalVariableMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            val (_, globalVarUsageCount) = node.jjtAccept(GlobalVariableVisitor(), null) as Pair<Int, Int>
            return globalVarUsageCount.toDouble()
        }
    }

    class ParameterPassMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            val parameterPassCount = node.jjtAccept(ParameterPassingVisitor(), MutableInt(0)) as MutableInt
            return parameterPassCount.value.toDouble()
        }
    }

    class RatioMetric {
        fun compute(globalVarUsageCount: Double, parameterPassCount: Double): Double {
            val total = parameterPassCount + globalVarUsageCount
            return if (total == 0.0) 0.0 else parameterPassCount / total
        }
    }
}