package nl.utwente.processing.pmdrules.metrics.messagepassing

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter
import net.sourceforge.pmd.lang.java.symboltable.VariableNameDeclaration
import nl.utwente.processing.pmdrules.utils.getContainingClass
import nl.utwente.processing.pmdrules.utils.isInManipulation
import nl.utwente.processing.pmdrules.utils.isMethodCall

class GlobalVariableVisitor : JavaParserVisitorAdapter() {
    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        // The main class is the first class declaration in the compilation unit
        val mainClass = node.getFirstDescendantOfType(ASTClassOrInterfaceDeclaration::class.java);
        // Global variables are defined in this main class
        val globalVariables = mainClass.scope.declarations
                // Filter on variables, we are not interested in methods and classes
                .filterKeys { it is VariableNameDeclaration }
                // Get all variable accesses that are not in the global (main class) scope
                .mapValues { it.value.filter { it.location.getContainingClass() != mainClass } }
                // Filter globals on the ones that are changed, so we don't mistake a global constant
                // for "fridge communication"
                .filter { it.value.any {
                    // Get the primary expression the variable was used in
                    val primExpr = it.location.getFirstParentOfType(ASTPrimaryExpression::class.java)
                    // Include this var if it is manipulated or a method is called on it (potentially mutating it)
                    primExpr?.isInManipulation() ?: false || primExpr?.isMethodCall ?: false } }
        // The number of keys is the number of mutated global variables
        val globalVarCount = globalVariables.count()
        // The summed number of items in the values is the number of global variable uses
        val globalVarUsageCount = globalVariables.entries.map { it.value.size }.sum()
        return super.visit(node, Pair(globalVarCount, globalVarUsageCount))
    }
}