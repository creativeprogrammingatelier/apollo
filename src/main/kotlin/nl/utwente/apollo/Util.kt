package nl.utwente.apollo

import kotlin.math.pow

fun s(a: Double, b: Double, x: Double) : Double {
    return 1 - (1 / ((x.pow(b) / a) + 1))
}

fun weightedAverage(vararg values: Pair<Double, Double?>) : Double {
    return values.sumByDouble { it.second ?: 0.0 } / values.sumByDouble { it.first }
}

fun tryParseDescription(description: String): Pair<Metrics, Double>? {
    val parts = description.split('=')
    if (parts.size == 2) {
        try {
            val metric = Metrics.valueOf(parts[0])
            val value = parts[1].replace(',', '.').toDouble()
            return Pair(metric, value)
        } catch (ex: Exception) {
            return null
        }
    } else {
        return null
    }
}