class FactorialWrapper() {
    public fun factorialOf(n: Int): Int {
       println("n is " + n.toString())
       return (
           if (n == 0) 1 else this.factorialOf(n - 1) * n
       )
    }
}
