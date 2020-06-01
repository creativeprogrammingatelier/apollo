package nl.utwente.processing.pmdrules.metrics.loops

import net.sourceforge.pmd.lang.java.ast.*
import nl.utwente.processing.pmdrules.utils.*

class LoopVisitor : JavaParserVisitorAdapter() {
    private fun toVariableUse(expr: ASTPrimaryExpression): VariableUse {
        return VariableUse(
                expr.deepImage(),
                expr.isInDirectArrayDereference(),
                expr.dereferencedArray()?.deepImage(),
                expr.isInManipulation(),
                expr.isInConstantManipulation()
        )
    }

    override fun visit(node: ASTForStatement, data: Any): Any {
        if (node.isForeach) {
            (data as MutableList<LoopPlan>).add(ForeachPlan())
        } else {
            val init = node.getFirstDescendantOfType(ASTForInit::class.java)
            val initVarDecl = init.getFirstDescendantOfType(ASTVariableDeclaratorId::class.java)
            val initVar =
                if (initVarDecl != null) {
                    initVarDecl.deepImage()
                } else {
                    val initAssignment = init.getFirstDescendantOfType(ASTStatementExpression::class.java)
                    if (initAssignment?.getFirstChildOfType(ASTAssignmentOperator::class.java) != null) {
                        initAssignment.getFirstChildOfType(ASTPrimaryExpression::class.java)?.deepImage()
                    } else {
                        null
                    }
                }
            val relation = node.condition.getFirstChildOfType(ASTRelationalExpression::class.java)
            val conditionType =
                if (relation != null) {
                    val names = relation.findDescendantsOfType(ASTPrimaryPrefix::class.java)
                    val containsArrayLength = names.any { it.deepImage().endsWith(".length") }
                    if (containsArrayLength)
                        ForPlan.ConditionType.ARRAY_LENGTH_RELATION
                    else
                        ForPlan.ConditionType.RELATION
                } else {
                    if (node.condition.getFirstChildOfType(ASTEqualityExpression::class.java) != null)
                        ForPlan.ConditionType.EQUALITY
                    else
                        ForPlan.ConditionType.OTHER
                }
            val loopingVariables =
                    node.body.findDescendantsOfType(ASTPrimaryExpression::class.java)
                            .filter { it.deepImage() == initVar }
                            .map { toVariableUse(it) }
                            .toSet()
            (data as MutableList<LoopPlan>).add(ForPlan(initVar, conditionType, loopingVariables))
        }
        return super.visit(node, data)
    }

    override fun visit(node: ASTWhileStatement, data: Any): Any {
        val relation = node.condition.getFirstChildOfType(ASTRelationalExpression::class.java)
        val equality = node.condition.getFirstChildOfType(ASTEqualityExpression::class.java)
        val conditionType = when {
            relation != null ->
                if (relation.findChildrenOfType(ASTPrimaryExpression::class.java).any {it.isNumeral()})
                    WhilePlan.ConditionType.NUMERAL_RELATION
                else
                    WhilePlan.ConditionType.RELATION
            equality != null ->
                if (equality.findChildrenOfType(ASTPrimaryExpression::class.java).any {it.isNumeral()})
                    WhilePlan.ConditionType.NUMERAL_EQUALITY
                else
                    WhilePlan.ConditionType.EQUALITY
            else ->
                WhilePlan.ConditionType.OTHER
        }
        val conditionVariables =
                node.condition.findDescendantsOfType(ASTPrimaryExpression::class.java)
                        .filter { it.isLowest() && !it.isLiteral() && !it.isLambda() }
        val loopingVariables =
                node.body.findDescendantsOfType(ASTPrimaryExpression::class.java)
                        .filter { conditionVariables.any { cd -> it.similar(cd) } }
                        .map { toVariableUse(it) }
                        .toSet()
        (data as MutableList<LoopPlan>).add(WhilePlan(conditionType, loopingVariables))
        return super.visit(node, data)
    }
}