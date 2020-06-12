package nl.utwente.apollo

import nl.utwente.apollo.pmd.ApolloPMDRunner
import nl.utwente.apollo.pmd.drawing.DrawingReportRule
import nl.utwente.apollo.pmd.loops.LoopReportRule
import nl.utwente.apollo.pmd.messagepassing.MessagePassingReportRule
import nl.utwente.apollo.pmd.oo.OoReportRule
import nl.utwente.processing.ProcessingFile
import nl.utwente.processing.ProcessingProject
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
                .map {p -> ProcessingFile(p.fileName.toString(), p.fileName.toString(), Files.readString(p)) }
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