package nl.utwente.processing.pmdrules.metrics.loops

abstract class LoopPlan(val type: Type) {
    enum class Type { WHILE, FOR, FOREACH }
    abstract fun isAppropriate(): Boolean
}

class WhilePlan(val conditionType: ConditionType) : LoopPlan(Type.WHILE) {
    enum class ConditionType {
        NUMERAL_COMPARISON,
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
