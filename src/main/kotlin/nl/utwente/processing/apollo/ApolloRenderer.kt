package nl.utwente.processing.apollo

import net.sourceforge.pmd.Report
import net.sourceforge.pmd.RuleViolation
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer
import nl.utwente.processing.pmdrules.metrics.Metrics
import nl.utwente.processing.pmdrules.metrics.drawing.DrawingReportRule
import nl.utwente.processing.pmdrules.metrics.loops.LoopReportRule
import nl.utwente.processing.pmdrules.metrics.messagepassing.MessagePassingReportRule
import nl.utwente.processing.pmdrules.metrics.oo.OoMetrics
import nl.utwente.processing.pmdrules.metrics.oo.OoReportRule
import nl.utwente.processing.pmdrules.metrics.tryParseDescription
import java.io.Writer

//class ApolloRenderer(val project: ProcessingProject) : AbstractIncrementingRenderer("Apollo", "") {
class ApolloRenderer() : AbstractIncrementingRenderer("Apollo", "") {

    val metrics = mutableMapOf<Metrics, Double>()
    private val violations = mutableListOf<RuleViolation>()

    fun getErrors(): List<Report.ProcessingError> {
        return this.errors.toList()
    }

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