package nl.utwente.apollo.pmd.drawing

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.metrics.AbstractJavaMetric
import net.sourceforge.pmd.lang.metrics.MetricOptions
import nl.utwente.apollo.s
import nl.utwente.processing.pmd.symbols.ProcessingApplet
import nl.utwente.processing.pmd.symbols.ProcessingAppletMethod
import nl.utwente.processing.pmd.symbols.ProcessingAppletMethodCategory
import nl.utwente.processing.pmd.symbols.ProcessingAppletMethodCategory.*

class DrawingMetrics {
    companion object {
        val coveredCategories = setOf(SHAPE_2D, SHAPE_ATTRIBUTES, TRANSFORM, COLOR_SETTING, COLOR_CR)
        val advancedCoveredCategories = setOf(TRANSFORM)
        val uncoveredCategories = ProcessingAppletMethodCategory.values().toSet().minus(coveredCategories)

        fun getDrawCalls(node: ASTCompilationUnit) : Set<ProcessingAppletMethod> {
            return node.jjtAccept(DrawingCallVisitor(), mutableSetOf<ProcessingAppletMethod>())
                    as MutableSet<ProcessingAppletMethod>
        }
    }

    class UsedDrawingsMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            return getDrawCalls(node).filter { it.category in coveredCategories }.size.toDouble()
        }

        fun computeProbability(coveredDrawCalls: Double): Double {
            return s(27748375.73, 8.22, coveredDrawCalls)
        }
    }

    class AdvancedDrawingsMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            return getDrawCalls(node).filter { it.category in advancedCoveredCategories }.size.toDouble()
        }

        fun computeProbability(advancedDrawCalls: Double): Double {
            return s(1.0, 4.25, advancedDrawCalls)
        }
    }

    class UncoveredDrawingsMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            return getDrawCalls(node).filter { it.category in uncoveredCategories }.size.toDouble()
        }

        fun computeProbability(uncoveredDrawCalls: Double): Double {
            return s(0.05, 1.82, uncoveredDrawCalls)
        }
    }
}
