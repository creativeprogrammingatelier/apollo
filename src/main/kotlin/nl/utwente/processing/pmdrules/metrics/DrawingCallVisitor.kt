package nl.utwente.processing.pmdrules.metrics

import nl.utwente.processing.pmdrules.utils.*
import nl.utwente.processing.pmdrules.symbols.ProcessingApplet
import nl.utwente.processing.pmdrules.symbols.ProcessingAppletMethodCategory

import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter

class DrawCallVisitor() : JavaParserVisitorAdapter() {
    class UsedDrawMethods() {
        val map: MutableMap<ProcessingAppletMethodCategory, MutableSet<String>> = mutableMapOf()
        fun add(method: String, category: ProcessingAppletMethodCategory) {
            val methods = map.getOrDefault(category, mutableSetOf())
            methods.add(method)
            map.putIfAbsent(category, methods)
        }
        fun getCountPerCategory() : Map<ProcessingAppletMethodCategory, Int> {
            return map.mapValues {entry -> entry.value.size}.toMap()
        }
    }

    val usedMethods = UsedDrawMethods()

    override fun visit(node: ASTPrimaryExpression, data: Any) : Any {
        if (node.isMethodCall) {
            val drawMethod = node.matches(*ProcessingApplet.DRAW_METHODS.toTypedArray())
            drawMethod?.let {method -> 
                (data as UsedDrawMethods).add(method.name, method.category)
            }
        }
        return super.visit(node, data)
    }
}