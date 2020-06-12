package nl.utwente.apollo.pmd

import nl.utwente.apollo.Metrics
import nl.utwente.processing.ProcessingProject
import nl.utwente.processing.pmd.PMDRunner

class ApolloPMDRunner {
    private val runner = PMDRunner("rulesets/apollo.xml")

    fun run(project: ProcessingProject): Map<Metrics, Double> {
        val renderer = ApolloRenderer()
        runner.Run(project, renderer)

        for (err in renderer.errors) {
            // TODO: better logging
            println("Error while running Apollo: ${err.msg}")
        }

        return renderer.metrics
    }
}