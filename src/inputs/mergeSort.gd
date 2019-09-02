fun merge(a: List<Int>, b: List<Int>): List<Int> {
  return when {
    a.isEmpty() -> b
	b.isEmpty() -> a
	a.first() < b.first() -> listOf(a.first()) + merge(a.drop(1), b)
	else -> listOf(a.first()) + merge(a, b.drop(1))
  }
}

fun mergeSort(array: List<Int>, startIndex: Int, endIndex: Int): List<Int> {
  if (array.count() < 2) {
    return array
  } else {
    val halfOfLength: Int = array.count() / 2
	return merge(mergeSort(array.sublist(0, halfOfLength)), mergeSort(array.drop(halfOfLength)))
  }
}

fun main() {
  val list: List<Int> = listOf(4, 2, 5, 1)
  val sortedList: List<Int> = mergeSort(list, 0, list.count())
  assert(sortedList == listOf(1, 2, 4, 5))
  println(sortedList)
}

main()