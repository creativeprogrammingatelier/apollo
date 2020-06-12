package nl.utwente.apollo.pmd.drawing

import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter
import nl.utwente.processing.pmd.symbols.ProcessingApplet
import nl.utwente.processing.pmd.symbols.ProcessingAppletMethod
import nl.utwente.processing.pmd.utils.isMethodCall
import nl.utwente.processing.pmd.utils.matches

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