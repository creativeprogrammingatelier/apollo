package nl.utwente.processing.apollo

import net.sourceforge.pmd.RuleViolation
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer
import nl.utwente.processing.pmdrules.metrics.Metrics
import nl.utwente.processing.pmdrules.metrics.drawing.DrawingReportRule
import nl.utwente.processing.pmdrules.metrics.loops.LoopReportRule
import nl.utwente.processing.pmdrules.metrics.tryParseDescription
import java.io.Writer

//class ApolloRenderer(val project: ProcessingProject) : AbstractIncrementingRenderer("Apollo", "") {
class ApolloRenderer() : AbstractIncrementingRenderer("Apollo", "") {

    val metrics = mutableMapOf<Metrics, Double>()
    val violations = mutableListOf<RuleViolation>()

    override fun defaultFileExtension(): String {
        return "";
    }

    // Renderers are required to provide a writer, but we don't want to write
    // anything to a write, so we use the NoWriter:
    /** Writer that does absolutely nothing  */
    private inner class NoWriter : Writer() {
        override fun write(cbuf: CharArray, off: Int, len: Int) {}
        override fun flush() {}
        override fun close() {}
    }

    override fun start() {
        super.start()
        this.setWriter(NoWriter())
    }

    override fun renderFileViolations(newViolations: MutableIterator<RuleViolation>) {
        while (newViolations.hasNext()) {
            val violation = newViolations.next()

            val metricValue = tryParseDescription(violation.description)
            if (metricValue != null) {
                metrics.put(metricValue.first, metricValue.second)
            } else {
                violations.add(violation)
            }

            println("Violation: ${violation.rule.name} - ${violation.description}")
        }
    }

    override fun end() {
        for (err in errors) {
            println("Error: ${err.msg}\n${err.detail.lines().map { "\t" + it }.joinToString("\n")}")
        }

        println("Violations:")
        for (violation in violations) {
            println(" - ${violation.rule.name}: ${violation.description}")
        }

        println("Metrics:")
        for ((metric, value) in metrics) {
            println(" - $metric = $value")
        }

        println("\nConclusions:")
        println("Drawing: ${DrawingReportRule.calculateFinal(metrics)}")
        println("Loops: ${LoopReportRule.calculateFinal(metrics)}")
    }
}