class Utils() {
    public fun composed(first: (Int) -> Int,
                        second: (Int) -> Int): (Int) -> Int {
        return #{ x: Int ->
            return second(first(x))
        }
    }
}

fun main(): String {
    val mulByFive = #{ a: Int -> return 5 * a }
    val add = #{ a: Int ->
        return #{ b: Int ->
            return a + b
        }
    }

    val increase = add(1)
    val incThanMulByFive = Utils().composed(increase, mulByFive)
    return incThanMulByFive(2).toString()
}
