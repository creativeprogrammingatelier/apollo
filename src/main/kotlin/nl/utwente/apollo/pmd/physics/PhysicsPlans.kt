package nl.utwente.apollo.pmd.physics

import net.sourceforge.pmd.lang.java.ast.ASTName
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.symboltable.NameDeclaration
import net.sourceforge.pmd.lang.symboltable.NameOccurrence
import nl.utwente.apollo.pmd.physics.PhysicsOperation.*
import nl.utwente.apollo.pmd.physics.PhysicsAtom.*
import nl.utwente.processing.pmd.utils.isMethodCall

object PhysicsPlans {
    val plans = listOf(
            PhysicsPlan("Apply acceleration",
                    setOf(PVectorMethod("velocity", "add", PVector("acceleration"))),
                    setOf(PVectorMethod("position", "add", PVector("velocity")))),

            PhysicsPlan("Apply force with mass",
                    setOf(PVectorMethod("force", "div", Float("mass"))),
                    setOf(PVectorMethod("acceleration", "add", PVector("force")))),

            PhysicsPlan("Calculate air resistance",
                    setOf(PVectorMethod("velocity", "mag"), PVectorMethod("velocity", "get")),
                    setOf(PVectorMethod("drag", "mult", Int("neg1")), PVectorMethod("drag", "normalize")),
                    setOf(PVectorMethod("drag", "mult", Float("dragMag")))),

            PhysicsPlan("Calculate friction",
                    setOf(PVectorMethod("velocity", "get")),
                    setOf(PVectorMethod("friction", "mult", Int("neg1")), PVectorMethod("friction", "normalize")),
                    setOf(PVectorMethod("friction", "mult", Float("frictionCoeff")))),

            PhysicsPlan("Calculate gravitational attraction",
                    setOf(PVectorMethod("myLocation", "sub", PVector("otherLocation"))),
                    setOf(PVectorMethod("force", "mag")),
                    setOf(PVectorMethod("force", "normalization")),
                    setOf(PVectorMethod("force", "mult", Float("strength"))))
    )


    fun getMatchingPlans(operations: List<ASTPrimaryExpression>, pVectorUses: List<Pair<NameOccurrence, NameDeclaration>>): List<PhysicsPlan> {
        return plans.filter { it.matches(operations, pVectorUses) }
    }
}

data class PhysicsPlan(val name: String, val plan: List<Set<PhysicsOperation>>) {
    constructor(name: String, vararg plan: Set<PhysicsOperation>) : this(name, plan.toList())

    fun matches(operations: List<ASTPrimaryExpression>, pVectorUses: List<Pair<NameOccurrence, NameDeclaration>>): Boolean {
        val planIter = plan.listIterator()
        var currentSet = planIter.next().toMutableSet()
        var nameMappings = mapOf<String, NameDeclaration>()

        // Find the first match in the current set of operations
        // If found, return the op and the mapped names, else return null
        fun findMatch(expr: ASTPrimaryExpression): Pair<PhysicsOperation, Map<String, NameDeclaration>>? {
            return currentSet.fold(null as Pair<PhysicsOperation, Map<String, NameDeclaration>>?) { res, next ->
                if (res != null) res
                else {
                    val match = next.matches(expr, nameMappings, pVectorUses)
                    if (match.first) Pair(next, match.second)
                    else null
                } }
        }

        // Remove the matching op from the plan set, return whether or not we're done
        fun removeMatchFromSet(match: PhysicsOperation): Boolean {
            currentSet.remove(match)
            return if (currentSet.isEmpty()) {
                if (planIter.hasNext()) {
                    currentSet = planIter.next().toMutableSet()
                    false
                } else true
            } else false
        }

        // Loop over all expressions in the list of operations
        for (expr in operations) {
            // Try to find a match with the current set
            val match = findMatch(expr)
            // If we have a match
            if (match != null) {
                // Remove the op from the plan and return true if the plan is done
                if (removeMatchFromSet(match.first)) return true
                // Set the new name mappings with the result from the match
                nameMappings = match.second
            }
        }

        // If we got here, the plan was not finished, so it doesn't match
        return false
    }
}

sealed class PhysicsOperation {
    data class PVectorMethod(val vector: String, val method: String, val args: List<PhysicsAtom>) : PhysicsOperation() {
        constructor(vector: String, method: String, vararg args: PhysicsAtom) : this(vector, method, args.toList())

        // TODO: this currently only checks methodname and vector, not arguments/parameters
        override fun matches(operation: ASTPrimaryExpression, nameMappings: Map<String, NameDeclaration>, pVectorUses: List<Pair<NameOccurrence, NameDeclaration>>): Pair<Boolean, Map<String, NameDeclaration>> {
            // Get the object and method name
            val name = operation.getFirstDescendantOfType(ASTName::class.java)
            // Get the declaration for this vector, if it exists
            val decl = pVectorUses.find { it.first.location == name }?.second
            // If the op is a methodcall and this name is a methodcall to the specified method and the PVector has a known declaration
            if (operation.isMethodCall && name?.image?.endsWith(".$method") == true && decl != null) {
                // Then this might be a match
                // Next we need to check if the defined vector already has a mapped declaration
                if (nameMappings.containsKey(vector)) {
                    // If so, it needs to be the same as the declaration for our current use
                    if (nameMappings[vector] == decl) {
                        return Pair(true, nameMappings)
                    } else {
                        return Pair(false, nameMappings)
                    }
                } else {
                    // If this name was not mapped, we can map it now
                    return Pair(true, nameMappings.plus(Pair(vector, decl)))
                }
            } else {
                // Otherwise, this doesn't match
                return Pair(false, nameMappings)
            }
        }
    }

    abstract fun matches(operation: ASTPrimaryExpression,
                         nameMappings: Map<String, NameDeclaration>,
                         pVectorUses: List<Pair<NameOccurrence, NameDeclaration>>)
            : Pair<Boolean, Map<String, NameDeclaration>>
}

sealed class PhysicsAtom {
    data class PVector(val name: String) : PhysicsAtom()
    data class Float(val name: String) : PhysicsAtom()
    data class Int(val name: String) : PhysicsAtom()
}