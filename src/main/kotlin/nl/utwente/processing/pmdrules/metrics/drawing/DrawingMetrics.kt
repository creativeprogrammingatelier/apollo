package nl.utwente.processing.pmdrules.metrics.drawing

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.metrics.AbstractJavaMetric
import net.sourceforge.pmd.lang.java.metrics.impl.AbstractJavaClassMetric
import net.sourceforge.pmd.lang.metrics.MetricOptions
import nl.utwente.processing.pmdrules.metrics.s
import nl.utwente.processing.pmdrules.symbols.ProcessingApplet
import nl.utwente.processing.pmdrules.symbols.ProcessingAppletMethod
import nl.utwente.processing.pmdrules.symbols.ProcessingAppletMethodCategory
import nl.utwente.processing.pmdrules.symbols.ProcessingAppletMethodCategory.*

class DrawingMetrics {
    companion object {
        val coveredCategories = setOf(SHAPE_2D, SHAPE_ATTRIBUTES, TRANSFORM, COLOR_SETTING, COLOR_CR)
        val advancedCoveredCategories = setOf(TRANSFORM)
        val uncoveredCategories = ProcessingAppletMethodCategory.values().toSet().minus(coveredCategories)

        val coveredMethods = ProcessingApplet.DRAW_METHODS.filter { it.category in coveredCategories }
        val uncoveredMethods = ProcessingApplet.DRAW_METHODS.filter { it.category in uncoveredCategories }

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
            val coveredDrawCalls = getDrawCalls(node).filter { it.category in coveredCategories }
            return coveredDrawCalls.size.toDouble() / coveredMethods.size.toDouble()
        }
    }

    class AdvancedDrawingsMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            val advancedDrawCalls = getDrawCalls(node).filter { it.category in advancedCoveredCategories }
            return s(70.0, 3.0, advancedDrawCalls.size.toDouble())
        }
    }

    class UncoveredDrawingsMetric : AbstractJavaMetric<ASTCompilationUnit>() {
        override fun supports(node: ASTCompilationUnit): Boolean {
            return true
        }

        override fun computeFor(node: ASTCompilationUnit, options: MetricOptions?): Double {
            val uncoveredDrawCalls = getDrawCalls(node).filter { it.category in uncoveredCategories }
            return uncoveredDrawCalls.size.toDouble() / uncoveredMethods.size.toDouble()
        }
    }
}
