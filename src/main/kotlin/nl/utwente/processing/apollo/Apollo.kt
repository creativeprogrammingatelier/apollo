package nl.utwente.processing.apollo

import net.sourceforge.pmd.*
import net.sourceforge.pmd.util.ClasspathClassLoader
import net.sourceforge.pmd.util.datasource.DataSource
import net.sourceforge.pmd.util.datasource.ReaderDataSource
import nl.utwente.atelier.pmd.PMDFile
import nl.utwente.processing.ProcessingProject
import nl.utwente.processing.pmdrules.metrics.Metrics
import nl.utwente.processing.pmdrules.metrics.drawing.DrawingReportRule
import nl.utwente.processing.pmdrules.metrics.loops.LoopReportRule
import nl.utwente.processing.pmdrules.metrics.messagepassing.MessagePassingReportRule
import nl.utwente.processing.pmdrules.metrics.oo.OoReportRule
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.BiPredicate
import java.util.stream.Collectors

fun main(args: Array<String>) {
    if (args.size < 1) {
        println("Usage: <project path>")
        return
    }

    val path = Path.of(args[0])
    val project = ProcessingProject(
        Files.find(path, 6, BiPredicate { p, attr -> attr.isRegularFile && p.fileName.toString().endsWith(".pde") })
                .map {p -> PMDFile(p.fileName.toString(), p.fileName.toString(), Files.readString(p)) }
                .collect(Collectors.toList()))

    val runner = ApolloPMDRunner()
    val metrics = runner.run(project)

    // Reporting
    println("Metrics:")
    for ((metric, value) in metrics) {
        println(" - $metric = $value")
    }

    println("\nConclusions:")
    println("Drawing: ${DrawingReportRule.calculateFinal(metrics)}")
    println("Loops: ${LoopReportRule.calculateFinal(metrics)}")
    println("OO: ${OoReportRule.calculateFinal(metrics)}")
    println("Message passing: ${MessagePassingReportRule.calculateFinal(metrics)}")
}

class ApolloPMDRunner() {
    private val config = PMDConfiguration()
    init {
        config.minimumPriority = RulePriority.LOW
        config.ruleSets = "rulesets/apollo.xml"
        config.isIgnoreIncrementalAnalysis = true
    }

    private val ruleSetFactory = RulesetsFactoryUtils.createFactory(config)

    fun run(project: ProcessingProject): Map<Metrics, Double> {
        val renderer = ApolloRenderer()
        renderer.start()

        val datasources: List<DataSource> = listOf(
                ReaderDataSource(StringReader(project.getJavaProjectCode()), "Processing.pde")
        )

        try {
            PMD.processFiles(
                    config,
                    ruleSetFactory,
                    datasources,
                    RuleContext(), listOf(renderer))
        } finally {
            val auxiliaryClassLoader = config.classLoader
            if (auxiliaryClassLoader is ClasspathClassLoader) {
                auxiliaryClassLoader.close()
            }
        }

        renderer.end()
        renderer.flush()
        return renderer.metrics
    }
}