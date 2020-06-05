package nl.utwente.processing.pmdrules.metrics.messagepassing

import net.sourceforge.pmd.lang.java.ast.*
import nl.utwente.processing.pmdrules.utils.*
import org.apache.commons.lang3.mutable.MutableInt

class ParameterPassingVisitor : JavaParserVisitorAdapter() {
    override fun visit(node: ASTPrimaryExpression, data: Any?): Any? {
        // We're only interested in method calls
        if (node.isMethodCall) {
            // The method name is the last part of the PrimaryPrefix
            // TODO: this is the full object.methodName thing
            val methodName = node.getFirstChildOfType(ASTPrimaryPrefix::class.java)
                    .findChildrenOfType(ASTName::class.java)
                    .last()
            // Method calls to methods defined in the program we're looking at
            // TODO: this doesn't actually get the method declaration, but the object variable
            val declaration = node.scope.findDeclaration(methodName)
            // And only if the method was defined in a different class
            if (declaration != null && declaration.node.getContainingClass() != node.getContainingClass()) {
                // Get the argument node for this method call
                val argumentNode = node.findChildrenOfType(ASTPrimarySuffix::class.java).stream()
                        .filter { s -> s.isArguments }.findFirst().orElse(null)
                // Then get the argument list
                val argumentList = argumentNode?.getFirstDescendantOfType(ASTArgumentList::class.java)
                // For all arguments in the argument list (or none, if there were no arguments)
                for (arg in argumentList?.children() ?: listOf()) {
                    // Find where the value was declared (and if it is a declarable value)
                    val argDecl = node.scope.findDeclaration(node.getFirstDescendantOfType(ASTName::class.java))
                    // If it was declared in the "current" class, we consider this to be parameter message passing
                    if (argDecl?.node?.getContainingClass() == node.getContainingClass()) {
                        // So then we increment the counter
                        (data as MutableInt).increment()
                    }
                }
            }
        }
        return super.visit(node, data)
    }
}