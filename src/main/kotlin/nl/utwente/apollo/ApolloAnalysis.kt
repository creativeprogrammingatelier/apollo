package nl.utwente.apollo

import nl.utwente.apollo.pmd.ApolloPMDRunner
import nl.utwente.apollo.pmd.drawing.DrawingReportRule
import nl.utwente.apollo.pmd.loops.LoopReportRule
import nl.utwente.apollo.pmd.messagepassing.MessagePassingReportRule
import nl.utwente.apollo.pmd.oo.OoReportRule
import nl.utwente.apollo.pmd.physics.PhysicsReportRule
import nl.utwente.processing.ProcessingFile
import nl.utwente.processing.ProcessingProject
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import java.util.stream.Collectors
import java.util.stream.Stream

fun main(args: Array<String>) {
    if (args.size < 1) {
        println("Usage: <project path>")
        return
    }


    val path = Path.of(args[0])
    val isPdeFile = BiPredicate<Path, BasicFileAttributes>
        { p, attr -> attr.isRegularFile && p.fileName.toString().endsWith(".pde") && !p.fileName.toString().startsWith(".") }
    val isPdeFolder = BiPredicate<Path, BasicFileAttributes>
        { p, attr -> attr.isDirectory && !p.equals(path) && !p.contains(Path.of("__MACOSX")) && Files.find(p, 1, isPdeFile).anyMatch({ _ -> true }) }
    val results = Files.find(path, 6, isPdeFolder)
            .map { folder -> Pair(path.relativize(folder).toString(), ProcessingProject(
                    Files.find(folder, 6, isPdeFile)
                    .map {p -> ProcessingFile(p.fileName.toString(), p.fileName.toString(), Files.readString(p)) }
                    .collect(Collectors.toList())))}
            .flatMap {
                try {
                    val runner = ApolloPMDRunner()
                    Stream.of(Pair(it.first, runner.run(it.second)))
                } catch (ex: Exception) {
                    println("Error while running " + it.first)
                    ex.printStackTrace()
                    println()
                    Stream.empty<Pair<String, Map<Metrics, Double>>>()
                } }

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
            Metrics.MESSAGEPASSING_RATIO,
            Metrics.PHYSICS_RAW_PVECTOR_OP_COUNT,
            Metrics.PHYSICS_RAW_RECOGNIZED_PLAN_COUNT)

    val file = path.resolve("analysis.csv")
    Files.deleteIfExists(file)
    val writer = BufferedWriter(FileWriter(file.toFile(), true))
    writer.write("project;" + metrics.joinToString(";"))
    writer.write(";RES_DRAWING;RES_LOOPS;RES_OO;RES_MESSAGEPASSING;RES_PHYSICS")
    writer.newLine()
    results.forEach {
        writer.write(it.first + ";")
        writer.write(metrics.joinToString(";") { metric -> it.second[metric]!!.toString() })

        writer.write(";" + DrawingReportRule.calculateFinal(it.second).toString())
        writer.write(";" + LoopReportRule.calculateFinal(it.second).toString())
        writer.write(";" + OoReportRule.calculateFinal(it.second).toString())
        writer.write(";" + MessagePassingReportRule.calculateFinal(it.second).toString())
        writer.write(";" + PhysicsReportRule.calculateFinal(it.second).toString())

        writer.newLine()
    }
    writer.close()

    println("Done.")
}