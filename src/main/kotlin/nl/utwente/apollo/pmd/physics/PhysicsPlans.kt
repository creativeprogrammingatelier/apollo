package nl.utwente.apollo.pmd.physics

import nl.utwente.apollo.pmd.physics.PhysicsOperation.*
import nl.utwente.apollo.pmd.physics.PhysicsAtom.*

object PhysicsPlans {
    private val applyAcceleration = PhysicsPlan("Apply acceleration",
            PVectorMethod("velocity", "add", PVectorV("acceleration")),
            PVectorMethod("position", "add", PVectorV("velocity")),
            PVectorUse("position", true))

    private val applyForce = PhysicsPlan("Apply force",
            PVectorMethod("force", "div", FloatV("mass")),
            PVectorMethod("acceleration", "add", PVectorV("force")),
            applyAcceleration)

    private val airDrag = PhysicsPlan("Calculate air drag",
        Assign(FloatV("speed"), PVectorMethod("velocity", "mag")),
        Assign(FloatV("dragMag"), Multiplication(FloatV("coeff"), FloatV("speed"), FloatV("speed"))),
        Assign(PVectorV("drag"), PVectorMethod("velocity", "get")),
        PVectorMethod("drag", "mult", IntC(-1)),
        PVectorMethod("drag", "normalize"),
        PVectorMethod("drag", "mult", FloatV("dragMag")))

    val plans = listOf(applyForce, applyAcceleration, airDrag)
}

sealed class PhysicsOperation {
    data class Assign(val result: PhysicsAtom, val expr: PhysicsOperation) : PhysicsOperation()
    data class PVectorMethod(val vector: String, val method: String, val args: List<PhysicsAtom>) : PhysicsOperation() {
        constructor(vector: String, method: String, vararg args: PhysicsAtom) : this(vector, method, args.toList())
    }
    data class PVectorUse(val vector: String, val inDrawing: Boolean) : PhysicsOperation()
    data class Multiplication(val args: List<PhysicsAtom>) : PhysicsOperation() {
        constructor(vararg args: PhysicsAtom) : this(args.toList())
    }

    data class PhysicsPlan(val name: String, val plan: List<PhysicsOperation>) : PhysicsOperation() {
        constructor(name: String, vararg plan: PhysicsOperation) : this(name, plan.toList())
    }
}

sealed class PhysicsAtom {
    data class PVectorV(val name: String) : PhysicsAtom()
    data class FloatV(val name: String) : PhysicsAtom()
    data class IntC(val value: Int) : PhysicsAtom()
}