package nl.utwente.processing.pmdrules.metrics.loops

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.metrics.AbstractJavaMetric
import net.sourceforge.pmd.lang.metrics.MetricOptions
import nl.utwente.processing.pmdrules.metrics.s

class LoopMetrics {
    companion object {
        fun getLoops(node: ASTCompilationUnit) : List<LoopPlan> {
            return node.jjtAccept(LoopVisitor(), mutableListOf<LoopPlan>())
                    as MutableList<LoopPlan>
        }
    }

    class LoopVarietyMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            val loops = getLoops(node)
            return s(1.0, 3.0, loops.map {it.type}.toSet().size.toDouble())
        }
    }

    class LoopSituationVarietyMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            return 0.0
        }
    }
}