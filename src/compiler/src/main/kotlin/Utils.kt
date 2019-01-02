package com.godel.compiler

fun IntRange.splitByPoints(points: Sequence<Int>) =
    sequence {
        var lastIndex = 0
        points.forEach { currentIndex ->
            if (lastIndex < currentIndex)
                yield(IntRange(lastIndex, currentIndex))
            lastIndex = currentIndex
        }
        if (lastIndex < endInclusive)
            yield(IntRange(lastIndex, endInclusive))
    }

fun String.splitInIndexes(indexes: Sequence<Int>): Sequence<String> =
    IntRange(0, this.length)        // the whole code
        .splitByPoints(indexes)     // split the whole code line to parts by giving breaking points
        .map {
            // IntRange are inclusive range, we want to use substring with exclusive range.
            IntRange(it.start, it.endInclusive - 1).let { exclusiveRange ->
                this.substring(exclusiveRange)
            }
        }

fun String.splitWithoutDeletingSeparator(separator: Regex): Sequence<String> {
    val breakingPoints =
        separator.findAll(this)
            .flatMap {
                // for each appearance of the separator,
                // we should split the string before it and after it
                sequenceOf(it.range.first, it.range.endInclusive + 1)
            }
    return this.splitInIndexes(breakingPoints)
}

operator fun String.times(n: Int): String = if (n == 0) "" else (this + this * (n - 1))

fun Int.formattedString() =
    toString().reversed().chunked(3).joinToString(",").reversed()