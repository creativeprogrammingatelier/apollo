package nl.utwente.processing.pmdrules.metrics.loops

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.metrics.AbstractJavaMetric
import net.sourceforge.pmd.lang.metrics.MetricOptions
import nl.utwente.processing.pmdrules.metrics.s
import kotlin.math.min

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
            return loops.map {it.type}.toSet().size.toDouble()
        }

        fun computeProbability(loopTypeCount: Double): Double {
            return return s(1.0, 3.0, loopTypeCount)
        }
    }

    class LoopSituationVarietyMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            val loops = getLoops(node)
            val whileCount =
                loops.filterIsInstance<WhilePlan>()
                        .map { Triple(it.conditionType,
                            it.loopingVariables.any { it.isInArrayDereference },
                            it.loopingVariables.any { it.isInManipulation }) }
                        .count()
            val forCount =
                loops.filterIsInstance<ForPlan>()
                        .map { Triple(it.conditionType,
                            it.iteratorUses.any { it.isInArrayDereference },
                            it.iteratorUses.any { it.isInManipulation }) }
                        .count()
            val foreachCount =
                min(1, loops.filterIsInstance<ForeachPlan>().count())
            return (whileCount + forCount + foreachCount).toDouble()
        }

        fun computeProbability(situationCount: Double): Double {
            return s(3.0, 1.5, situationCount)
        }
    }

    class LoopAssessmentMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit?): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            val loops = getLoops(node)
            return if (loops.isEmpty())
                0.0
            else
                loops.count { it.isAppropriate() }.toDouble() / loops.count().toDouble()
        }
    }
}