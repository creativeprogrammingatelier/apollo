package nl.utwente.apollo.pmd.oo

import net.sourceforge.pmd.RuleViolation
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.metrics.AbstractJavaMetric
import net.sourceforge.pmd.lang.metrics.MetricOptions
import nl.utwente.apollo.s
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
            return s(0.05, 1.82, count)
        }
    }

    class SmellMetric {
        companion object {
            private val OO_RULES = listOf("DecentralizedDrawingRule", "DecentralizedEventHandlingRule",
                    "DrawingStateChangeRule", "GodClassRule", "LongMethodRule", "StatelessClassRule")
        }

        fun computeFor(violations: List<RuleViolation>) : Double {
            return violations.filter { it.rule.name in OO_RULES }.count().toDouble()
        }

        fun computeProbability(classCount: Double, count: Double): Double {
            return if (classCount == 0.0) 0.0
            else s(0.11, -3.21, count / classCount)
        }
    }
}