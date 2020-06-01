package nl.utwente.processing.pmdrules.metrics.oo

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter
import org.apache.commons.lang3.mutable.MutableInt

class ClassVisitor(val minMethods: Int) : JavaParserVisitorAdapter() {
    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any?): Any {
        if (node.typeKind == ASTAnyTypeDeclaration.TypeKind.CLASS) {
            val methods = node.findDescendantsOfType(ASTMethodDeclaration::class.java)
            if (methods.count() >= minMethods) {
                (data as MutableInt).increment()
            }
        }
        return super.visit(node, data)
    }
}