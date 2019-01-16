package com.godel.compiler

fun IntRange.splitByPoints(points: Sequence<Int>) =
    sequence {
        var lastIndex = 0
        points.forEach { currentIndex ->
            // for each index,
            // yield a range from the previous index (or the start) to it.
            if (lastIndex < currentIndex) { // it's not an empty range
                yield(IntRange(lastIndex, currentIndex))
                lastIndex = currentIndex
            }
        }
        //  yield the range from the last index till the end of the string (if the range exists)
        if (lastIndex < endInclusive)
            yield(IntRange(lastIndex, endInclusive))
    }

// Assumption: indexes are sorted in ascending order
fun String.splitInIndexes(indexes: Sequence<Int>) =
    IntRange(0, this.length)        // the whole code
        .splitByPoints(indexes)     // split the whole code line to parts by giving breaking points
        .map {
            // String.toString(IntRange) actually returns
            // the value of this.substring(it.start, it.endInclusive + 1)
            this.substring(it.start, it.endInclusive)
        }

// Takes a sequence and a predicate
// Returns a the original sequence split by the predicate.
// The labels mainSequence and subSequence are used only by the IDE and aren't necessary to the code
fun <T : Any> Sequence<T>.split(predicate: (T) -> Boolean): Sequence<Sequence<T>> {
    val iterator = this@split.iterator()
    return generateSequence mainSequence@{
        if (iterator.hasNext())
            generateSequence subSequence@{
                val nextValue = iterator.nextOrNull()
                nextValue?.takeUnless(predicate)
            }
        else null
    }
}

fun <T> Iterator<T>.nextOrNull() = if (hasNext()) next() else null

operator fun String.times(n: Int): String = if (n == 0) "" else (this + this * (n - 1))

fun Int.formattedString() =
    toString().reversed().chunked(3).joinToString(",").reversed()