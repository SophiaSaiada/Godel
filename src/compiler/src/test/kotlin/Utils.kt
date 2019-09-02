
import io.kotlintest.shouldBe

infix fun <T, U : T> Sequence<T>.seqShouldBe(any: Sequence<U>?) =
    this.toList() shouldBe any?.toList()
