package nl.utwente.processing.pmdrules.metrics.loops

abstract class LoopPlan(val type: Type) {
    enum class Type { WHILE, FOR, FOREACH }

    abstract fun isAppropriate(): Boolean
}

data class VariableUse(
        val image: String,
        val isInArrayDereference: Boolean,
        val dereferencedArray: String?,
        val isInManipulation: Boolean,
        val inConstantManipulation: Boolean) {
}

class WhilePlan(val conditionType: ConditionType, val loopingVariables: Set<VariableUse>) : LoopPlan(Type.WHILE) {
    enum class ConditionType {
        NUMERAL_RELATION,
        RELATION,
        NUMERAL_EQUALITY,
        EQUALITY,
        OTHER
    }

    override fun isAppropriate(): Boolean {
        return true
    }
}

class ForPlan : LoopPlan(Type.FOR) {

    override fun isAppropriate(): Boolean {
        return true
    }
}

class ForeachPlan() : LoopPlan(Type.FOREACH) {

    override fun isAppropriate(): Boolean {
        return true
    }
}
