package nl.utwente.apollo

import nl.utwente.apollo.pmd.ApolloPMDRunner
import nl.utwente.processing.ProcessingFile
import nl.utwente.processing.ProcessingProject
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.BiPredicate
import java.util.stream.Collectors

fun main(args: Array<String>) {
    if (args.size < 1) {
        println("Usage: <project path>")
        return
    }

    val runner = ApolloPMDRunner()

    val path = Path.of(args[0])
    val results = Files.find(path, 1, BiPredicate { p, attr -> attr.isDirectory && !p.equals(path) })
            .map { folder -> Pair(folder, ProcessingProject(
                    Files.find(folder, 6, BiPredicate { p, attr -> attr.isRegularFile && p.fileName.toString().endsWith(".pde") })
                    .map {p -> ProcessingFile(p.fileName.toString(), p.fileName.toString(), Files.readString(p)) }
                    .collect(Collectors.toList())))}
            .map { Pair(it.first, runner.run(it.second)) }

    val metrics = listOf(
            Metrics.DRAWING_RAW_COVERED_COUNT,
            Metrics.DRAWING_RAW_ADVANCED_COUNT,
            Metrics.DRAWING_RAW_UNCOVERED_COUNT,
            Metrics.LOOP_RAW_TYPE_COUNT,
            Metrics.LOOP_RAW_SITUATION_COUNT,
            Metrics.LOOP_ASSESSMENT,
            Metrics.OO_RAW_CLASS_COUNT,
            Metrics.OO_RAW_SMELL_COUNT,
            Metrics.MESSAGEPASSING_RAW_GLOBAL_COUNT,
            Metrics.MESSAGEPASSING_RAW_PARAMETER_COUNT,
            Metrics.MESSAGEPASSING_RATIO)

    val file = path.resolve("analysis.csv")
    val writer = BufferedWriter(FileWriter(file.toFile(), true))
    writer.write("project," + metrics.joinToString(","))
    writer.newLine()
    results.forEach {
        writer.write(it.first.fileName.toString() + ",")
        writer.write(metrics.joinToString(",") { metric -> it.second[metric]!!.toString() })
        writer.newLine()
    }
    writer.close()

    println("Done.")
}