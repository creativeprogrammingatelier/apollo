package nl.utwente.processing.pmdrules.metrics.loops

import net.sourceforge.pmd.lang.java.ast.ASTForStatement
import net.sourceforge.pmd.lang.java.ast.ASTRelationalExpression
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter

class LoopVisitor : JavaParserVisitorAdapter() {
    override fun visit(node: ASTForStatement, data: Any): Any {
        return super.visit(node, data)
    }

    override fun visit(node: ASTWhileStatement, data: Any): Any {
        val condition = node.condition.getFirstChildOfType(ASTRelationalExpression::class.java)
        return super.visit(node, data)
    }
}