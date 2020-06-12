package nl.utwente.apollo.pmd.loops

abstract class LoopPlan(val type: Type) {
    enum class Type { WHILE, FOR, FOREACH }

    abstract fun isAppropriate(): Boolean
}

data class VariableUse(
        val image: String,
        val isInArrayDereference: Boolean,
        val dereferencedArray: String?,
        val isInManipulation: Boolean,
        val isInConstantManipulation: Boolean) {
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
        return conditionType != ConditionType.NUMERAL_RELATION
                || loopingVariables.none { it.isInConstantManipulation }
    }
}

class ForPlan(val iteratorImage: String?, val conditionType: ConditionType, val iteratorUses: Set<VariableUse>) : LoopPlan(Type.FOR) {
    enum class ConditionType {
        ARRAY_LENGTH_RELATION,
        RELATION,
        EQUALITY,
        OTHER
    }

    override fun isAppropriate(): Boolean {
        return iteratorImage != null
                && (conditionType != ConditionType.ARRAY_LENGTH_RELATION
                    || iteratorUses.any { !it.isInArrayDereference })
    }
}

class ForeachPlan() : LoopPlan(Type.FOREACH) {

    override fun isAppropriate(): Boolean {
        // If you use a foreach, then it is most likely the most appropriate,
        // since there is no more specific type of loop that could be better
        return true
    }
}
