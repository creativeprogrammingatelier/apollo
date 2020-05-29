package nl.utwente.processing.apollo

import net.sourceforge.pmd.*
import net.sourceforge.pmd.util.ClasspathClassLoader
import net.sourceforge.pmd.util.datasource.DataSource
import net.sourceforge.pmd.util.datasource.ReaderDataSource
import nl.utwente.atelier.pmd.PMDFile
import nl.utwente.processing.ProcessingProject
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

    val config = PMDConfiguration()
    config.minimumPriority = RulePriority.LOW
    config.ruleSets = "rulesets/apollo.xml"
    config.isIgnoreIncrementalAnalysis = true
    val ruleSetFactory = RulesetsFactoryUtils.createFactory(config)

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
}