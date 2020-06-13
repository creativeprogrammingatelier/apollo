package nl.utwente.apollo.pmd.physics

import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.java.ast.*
import net.sourceforge.pmd.lang.symboltable.NameDeclaration
import net.sourceforge.pmd.lang.symboltable.NameOccurrence
import net.sourceforge.pmd.lang.symboltable.Scope
import nl.utwente.processing.pmd.symbols.ProcessingApplet
import nl.utwente.processing.pmd.utils.deepImage
import nl.utwente.processing.pmd.utils.isMethodCall
import nl.utwente.processing.pmd.utils.matches
import nl.utwente.processing.pmd.utils.matchesBuiltinInstanceCall

class PVectorVisitor : JavaParserVisitorAdapter() {
    companion object {
        fun Scope.filterDeclarationsRecursive(filter: (decl: NameDeclaration) -> Boolean): Map<NameDeclaration, List<NameOccurrence>> {
            val local = this.declarations.filterKeys(filter).mapValues { it.value.toList() }
            val children = this.declarations
                    .filter { it.key.scope.parent == this }
                    .map { it.key.scope.filterDeclarationsRecursive(filter) }
                    .fold(mapOf<NameDeclaration, List<NameOccurrence>>(), { a, b -> a + b })
            return local + children
        }

        val Node.isVariableDeclaration
            get() = this is ASTFieldDeclaration || this is ASTLocalVariableDeclaration

        val Node.isPVectorDeclaration: Boolean
            get() {
                val type = this.getFirstChildOfType(ASTType::class.java)?.deepImage() ?:
                        this.getFirstDescendantOfType(ASTVariableInitializer::class.java)
                            ?.getFirstChildOfType(ASTExpression::class.java)
                            ?.getFirstChildOfType(ASTPrimaryExpression::class.java)
                            ?.getFirstChildOfType(ASTAllocationExpression::class.java)
                            ?.getFirstChildOfType(ASTClassOrInterfaceType::class.java)
                            ?.image
                return type == "PVector"
            }

        fun Scope.getPVectorDeclarations(): Map<NameDeclaration, List<NameOccurrence>> {
            return this.filterDeclarationsRecursive { it.node.parent.parent.isVariableDeclaration && it.node.parent.parent.isPVectorDeclaration }
        }

        val Node.isInDrawCall: Boolean
            get() = this.getParentsOfType(ASTPrimaryExpression::class.java)
                    .any { it.isMethodCall && it.matches(*ProcessingApplet.DRAW_METHODS.toTypedArray()) != null }
    }

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        // The main class is the first class declaration in the compilation unit
        val mainClass = node.getFirstDescendantOfType(ASTClassOrInterfaceDeclaration::class.java)
        val pVectors = mainClass.scope.getPVectorDeclarations()
        val uses = pVectors.flatMap { it.value.map { v -> Pair(v, it.key) } }
        val operationCount = uses
                .filter {
                    it.first.location.getFirstParentOfType(ASTPrimaryExpression::class.java)
                    ?.matchesBuiltinInstanceCall(ProcessingApplet.PVECTOR_INSTANCE_MODIFICATION_METHODS) != null }
                .count()
        val blocks = uses.groupBy { it.first.location.getFirstParentOfType(ASTBlock::class.java) }
        // TODO: plan detection
        return super.visit(node, operationCount)
    }
}