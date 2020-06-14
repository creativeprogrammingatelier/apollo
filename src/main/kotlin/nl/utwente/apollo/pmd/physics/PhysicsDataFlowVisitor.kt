package nl.utwente.apollo.pmd.physics

import net.sourceforge.pmd.lang.dfa.pathfinder.CurrentPath
import net.sourceforge.pmd.lang.dfa.pathfinder.DAAPathFinder
import net.sourceforge.pmd.lang.dfa.pathfinder.Executable
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter

class PhysicsDataFlowVisitor : JavaParserVisitorAdapter(), Executable {
    override fun visit(node: ASTMethodDeclaration, data: Any?): Any? {
        val flowNode = node.dataFlowNode.flow[0]
        DAAPathFinder(flowNode, this).run()
        return super.visit(node, data)
    }

    override fun execute(path: CurrentPath) {
        for (node in path) {
            if (node.variableAccess != null) {
                for (va in node.variableAccess) {
                    println("Variable access: ${va.variableName} (Type: ${va.accessType}")
                }
            }
        }
    }
}