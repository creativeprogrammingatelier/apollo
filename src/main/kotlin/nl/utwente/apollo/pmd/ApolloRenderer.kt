package nl.utwente.apollo.pmd

import net.sourceforge.pmd.RuleViolation
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer
import nl.utwente.apollo.Metrics
import nl.utwente.apollo.pmd.oo.OoMetrics
import nl.utwente.apollo.tryParseDescription
import java.io.Writer

class ApolloRenderer : AbstractIncrementingRenderer("Apollo", "") {

    private val violations = mutableListOf<RuleViolation>()
    val metrics = mutableMapOf<Metrics, Double>()
    val errors get() = super.errors

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
                metrics[metricValue.first] = metricValue.second
            } else {
                violations.add(violation)
            }
        }
    }

    override fun end() {
        // Final calculations
        val ooSmellMetric = OoMetrics.SmellMetric()
        metrics[Metrics.OO_RAW_SMELL_COUNT] = ooSmellMetric.computeFor(violations)
        metrics[Metrics.OO_SMELLS] = ooSmellMetric.computeProbability(
                metrics[Metrics.OO_RAW_CLASS_COUNT]!!, metrics[Metrics.OO_RAW_SMELL_COUNT]!!)
    }
}