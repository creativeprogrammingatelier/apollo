package nl.utwente.processing.pmdrules.metrics.drawing

import nl.utwente.processing.pmdrules.utils.*
import nl.utwente.processing.pmdrules.symbols.ProcessingApplet

import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter
import nl.utwente.processing.pmdrules.symbols.ProcessingAppletMethod

class DrawingCallVisitor() : JavaParserVisitorAdapter() {
    override fun visit(node: ASTPrimaryExpression, data: Any) : Any {
        if (node.isMethodCall) {
            val drawMethod = node.matches(*ProcessingApplet.DRAW_METHODS.toTypedArray())
            drawMethod?.let {method -> 
                (data as MutableSet<ProcessingAppletMethod>).add(method)
            }
        }
        return super.visit(node, data)
    }
}