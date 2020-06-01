package nl.utwente.processing.pmdrules.metrics.oo

import net.sourceforge.pmd.RuleViolation
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.metrics.AbstractJavaMetric
import net.sourceforge.pmd.lang.metrics.MetricOptions
import nl.utwente.processing.pmdrules.metrics.s
import org.apache.commons.lang3.mutable.MutableInt
import java.lang.Double.max
import kotlin.math.sqrt

class OoMetrics {
    companion object {
        fun countClasses(node: ASTCompilationUnit) : Int {
            return (node.jjtAccept(ClassVisitor(2), MutableInt(0)) as MutableInt).value
        }
    }

    class ClassCountMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            return countClasses(node).toDouble()
        }

        fun computeProbability(count: Double): Double {
            return s(1.0, 2.0, count)
        }
    }

    class SmellMetric {
        companion object {
            private val OO_RULES = listOf("DecentralizedDrawingRule", "DecentralizedEventHandlingRule",
                    "DrawingStateChangeRule", "GodClassRule", "LongMethodRule", "StatelessClassRule")
        }

        fun compute(classCount: Double, violations: List<RuleViolation>) : Double {
            val count = violations.filter { it.rule.name in OO_RULES }.count()
            return 1 / max(1.0, count / sqrt(classCount))
        }
    }
}