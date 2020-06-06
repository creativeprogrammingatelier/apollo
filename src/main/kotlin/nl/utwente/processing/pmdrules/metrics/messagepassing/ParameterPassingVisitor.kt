package nl.utwente.processing.pmdrules.metrics.messagepassing

import net.sourceforge.pmd.lang.java.ast.*
import net.sourceforge.pmd.lang.java.symboltable.ClassNameDeclaration
import net.sourceforge.pmd.lang.java.symboltable.MethodNameDeclaration
import net.sourceforge.pmd.lang.java.symboltable.VariableNameDeclaration
import net.sourceforge.pmd.lang.symboltable.NameDeclaration
import net.sourceforge.pmd.lang.symboltable.NameOccurrence
import net.sourceforge.pmd.lang.symboltable.Scope
import nl.utwente.processing.pmdrules.utils.findDeclaration
import nl.utwente.processing.pmdrules.utils.getContainingClass
import org.apache.commons.lang3.mutable.MutableInt


class ParameterPassingVisitor : JavaParserVisitorAdapter() {
    companion object {
        /** Get all method declarations in this scope with a list of all calls to these methods */
        fun Scope.getMethodDeclarations(): Map<NameDeclaration, List<NameOccurrence>> {
            // Get local method declarations
            val localMethods = this.declarations.filterKeys { it is MethodNameDeclaration }
            // From all local declarations
            val subClassMethods = this.declarations.keys
                    // Get the inner class declarations
                    .filterIsInstance<ClassNameDeclaration>()
                    // For each of these recursively get all method declarations
                    .map { it.scope.getMethodDeclarations() }
                    // Reduce the list of maps into a single map
                    .fold(mapOf<NameDeclaration, List<NameOccurrence>>(), { x, y -> x + y })
            // Return both local and inner class methods
            return localMethods + subClassMethods
        }
    }

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        // The main class is the first class declaration in the compilation unit
        val mainClass = node.getFirstDescendantOfType(ASTClassOrInterfaceDeclaration::class.java);
        // Get all methods declared in this program
        val methods = mainClass.scope.getMethodDeclarations()
        // For each method declaration
        for ((decl, calls) in methods) {
            // And for each call to that method
            for (call in calls) {
                // If the call is in a different class
                if (call.location.getContainingClass() != decl.node.getContainingClass()) {
                    // Get the expression in which the call takes place
                    val expr = call.location.getFirstParentOfType(ASTPrimaryExpression::class.java)
                    // Get the arguments for the method call
                    val args =
                        when (val loc = call.location) {
                            // When the location is an ASTName
                            is ASTName -> {
                                // The arguments are in the first arguments suffix of the expression
                                val argNode = expr.findChildrenOfType(ASTPrimarySuffix::class.java).firstOrNull { it.isArguments }
                                // As the children of the ArgumentList node
                                argNode?.getFirstDescendantOfType(ASTArgumentList::class.java)?.children()
                                        ?: listOf()
                            }
                            // When the call location is a PrimarySuffix (e.g. in rockets[i].update()
                            is ASTPrimarySuffix -> {
                                var next = false
                                var args: List<JavaNode>? = null
                                // Loop over all parts of the expression
                                for (part in expr.children()) {
                                    // If this part is the location of the method call,
                                    // one of the next part should be the arguments
                                    if (part == loc) next = true;
                                    // So if we have seen the location, check if this node is an arguments suffix
                                    if (next && (part as ASTPrimarySuffix).isArguments) {
                                        // If so, then the children of the ArgumentList are the arguments for this call
                                        args = part.getFirstDescendantOfType(ASTArgumentList::class.java)?.children()?.toList()
                                        // And we can stop looping over the expression
                                        break
                                    }
                                }
                                // If the ArgumentList was not found, this method call does not have arguments
                                args ?: listOf()
                            }
                            else -> {
                                println("Unrecognized method call type: ${loc.javaClass.typeName}")
                                listOf()
                            }
                        }
                    // For each argument in the method call
                    for (arg in args) {
                        // Find where the value was declared (and if it is a declarable value)
                        val argDecl = node.scope.findDeclaration(node.getFirstDescendantOfType(ASTName::class.java), VariableNameDeclaration::class.java)
                        // If it was declared in the "current" class, we consider this to be parameter message passing
                        if (argDecl?.node?.getContainingClass() == node.getContainingClass()) {
                            // So then we increment the counter
                            (data as MutableInt).increment()
                        }
                    }
                }
            }
        }

        return super.visit(node, data)
    }
}