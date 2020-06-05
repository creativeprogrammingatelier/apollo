package nl.utwente.processing.pmdrules.metrics.messagepassing

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.metrics.AbstractJavaMetric
import net.sourceforge.pmd.lang.metrics.MetricOptions
import org.apache.commons.lang3.mutable.MutableInt

class MessagePassingMetrics {

    class RatioMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            val (_, globalVarUsageCount) = node.jjtAccept(GlobalVariableVisitor(), null) as Pair<Int, Int>
            val parameterPassCount = node.jjtAccept(ParameterPassingVisitor(), MutableInt(0)) as MutableInt
            return parameterPassCount.value.toDouble() / (parameterPassCount.value + globalVarUsageCount)
        }
    }
}