class Composed(
    public val first: (Int) -> Int,
    public val second: (Int) -> Int
) {
    public fun run(x: Int): Int {
        return this.second(this.first(x))
    }
}

class Adder(public val constant: Int) {
    public fun add(x: Int): Int {
        return this.constant + x
    }
}

fun main(): String {
    val n = 3
    val nMultiplier = #{ a: Int -> return n * a }
    val twoAdder = Adder(2)
    val composed = Composed(twoAdder.add, nMultiplier)
    return composed.run(2).toString()
}
