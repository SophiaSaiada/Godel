package com.godel.compiler

const val APOSTROPHES = "\""

fun IntRange.splitByPoints(points: Sequence<Int>): List<IntRange> {
    val rangesBetweenEachPointAndTheirPredecessor =
        points.fold(emptyList()) { ranges: List<IntRange>, currentPoint: Int ->
            val startIndex = ranges.lastOrNull()?.last ?: 0
            if (startIndex < currentPoint)
                ranges + listOf(IntRange(startIndex, currentPoint))
            else ranges
        }
    return points.lastOrDefault(0).let { startIndex ->
        if (startIndex < endInclusive)
            rangesBetweenEachPointAndTheirPredecessor + listOf(IntRange(startIndex, endInclusive))
        else rangesBetweenEachPointAndTheirPredecessor
    }
}

fun String.splitInIndexes(indexes: Sequence<Int>): List<String> {
    val substringRanges = IntRange(0, this.length).splitByPoints(indexes)
    return substringRanges
        .map {
            IntRange(it.start, it.endInclusive - 1).let { exclusiveRange -> this.substring(exclusiveRange) }
        }
}

fun String.splitWithoutDeletingSeparator(separator: Regex): List<String> {
    val breakingPoints =
        separator.findAll(this)
            .flatMap { sequenceOf(it.range.first, it.range.endInclusive + 1) }
    return this.splitInIndexes(breakingPoints)
}

fun <T> Sequence<T>.lastOrDefault(default: T) = this.lastOrNull() ?: default
inline fun <T> List<T>.mapLast(transform: (T) -> T) = this.dropLast(1) + transform(this.last())
