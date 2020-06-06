package nl.utwente.processing.pmdrules.metrics.oo

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter
import org.apache.commons.lang3.mutable.MutableInt

class ClassVisitor(val minMethods: Int) : JavaParserVisitorAdapter() {
    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any?): Any {
        // Only look at classes that are nested (so we don't count the wrapping Processing class)
        if (node.typeKind == ASTAnyTypeDeclaration.TypeKind.CLASS && node.isNested) {
            // Find all method declarations in this class
            val methods = node.findDescendantsOfType(ASTMethodDeclaration::class.java)
            // If this class satisfies the minimum amount of methods
            if (methods.count() >= minMethods) {
                // Then increment the class count by one
                (data as MutableInt).increment()
            }
        }
        return super.visit(node, data)
    }
}