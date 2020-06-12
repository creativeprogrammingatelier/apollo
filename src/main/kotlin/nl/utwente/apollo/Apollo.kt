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

object Apollo {
    fun verbalize(probability: Double): String {
        return when {
            probability <= 0.20  -> "improbable"
            probability <= 0.375 -> "uncertain"
            probability <= 0.675 -> "fifty-fifty"
            probability <= 0.80  -> "expected"
            probability <= 0.95  -> "probable"
            else                 -> "almost certain"
        }
    }

    fun formatResults(metrics: Map<Metrics, Double>): String {
        val s = StringBuilder()
        s.appendln("Based on this submission, Apollo thinks it is")

        s.append("- ")
        s.append(verbalize(DrawingReportRule.calculateFinal(metrics)))
        s.appendln(" that the student can write a program that uses graphical commands to draw to the screen")

        s.append("- ")
        s.append(verbalize(LoopReportRule.calculateFinal(metrics)))
        s.appendln(" that the student can write a program that uses looping constructs for repetition")

        s.append("- ")
        s.append(verbalize(OoReportRule.calculateFinal(metrics)))
        s.appendln(" that the student can compose a program using classes, objects and methods to structure the code in an object-oriented way")

        s.append("- ")
        s.append(verbalize(MessagePassingReportRule.calculateFinal(metrics)))
        s.appendln(" that the student can implement message passing to enable communication between classes in a complex program")

        return s.toString()
    }
}

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
    print(Apollo.formatResults(metrics))
}

