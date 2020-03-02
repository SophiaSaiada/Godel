class IntUtils() {
  public fun factorial(n: Int): Int {
    val output = if (n == 0) 1 else n * this.factorial(n - 1)
    println("factorial(" + n.toString() + "): " + output.toString())
    return output
  }
}

fun main(): String {
    val intUtils = IntUtils()
    val input = 5
    val output = intUtils.factorial(input).toString()
    return output
}