package nl.utwente.apollo.pmd.drawing

import nl.utwente.processing.pmd.utils.*
import nl.utwente.processing.pmd.symbols.ProcessingApplet

import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter
import nl.utwente.processing.pmd.symbols.ProcessingAppletMethod

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