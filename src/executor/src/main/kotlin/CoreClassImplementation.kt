import Executor.Object.Primitive as Primitive
import ASTNode.Type.Core as Core

class NativeFunction(
    val value: (Executor.Object?, List<Executor.Object>) -> Executor.Object
)
typealias PrimitiveCoreInt = Primitive.CoreInt
typealias PrimitiveCoreFloat = Primitive.CoreFloat

val coreClassImplementations: Map<ASTNode.Type, Map<String, NativeFunction>> = mapOf(
    Core.boolean to mapOf(
        "&&" to NativeFunction { self, parameters ->
            val (first, second) = (self!! + parameters).map { it as Primitive.CoreBoolean }.map { it.innerValue }
            Primitive.CoreBoolean(first && second)
        },
        "&&" to NativeFunction { self, parameters ->
            val (first, second) = (self!! + parameters).map { it as Primitive.CoreBoolean }.map { it.innerValue }
            Primitive.CoreBoolean(first || second)
        }
    ) + getBasicFunctionsOfType<Boolean>(),
    Core.int to getBasicFunctionsOfType<Int>() + getArithmeticFunctionsOfType(
        ::PrimitiveCoreInt,
        { first: Int, second: Int -> first + second },
        { first: Int, second: Int -> first - second },
        { first: Int, second: Int -> first * second },
        { first: Int, second: Int -> first / second }
    ),
    Core.float to getBasicFunctionsOfType<Float>() + getArithmeticFunctionsOfType(
        ::PrimitiveCoreFloat,
        { first: Float, second: Float -> first + second },
        { first: Float, second: Float -> first - second },
        { first: Float, second: Float -> first * second },
        { first: Float, second: Float -> first / second }
    ),
    Core.string to getBasicFunctionsOfType<String>() + mapOf(
        "+" to NativeFunction { self, parameters ->
            val (first, second) = (self!! + parameters).map { it as Primitive.CoreString }.map { it.innerValue }
            Primitive.CoreString(first + second)
        },
        "length" to NativeFunction { self, _ ->
            val selfTyped = (self as Primitive.CoreString).innerValue
            Primitive.CoreInt(selfTyped.length)
        }
    ),
    Core.unit to emptyMap()
)

private inline fun <reified R> getBasicFunctionsOfType() =
    mapOf(
        "==" to NativeFunction { self, parameters ->
            val (first, second) = (self!! + parameters).map { it as Primitive<*> }.map { it.innerValue as R }
            Primitive.CoreBoolean(first == second)
        },
        "!=" to NativeFunction { self, parameters ->
            val (first, second) = (self!! + parameters).map { it as Primitive<*> }.map { it.innerValue as R }
            Primitive.CoreBoolean(first != second)
        },
        "toString" to NativeFunction { self, _ ->
            val selfTyped = (self as Primitive<*>).innerValue as R
            Primitive.CoreString(selfTyped.toString())
        }
    )

private inline fun <reified T> getArithmeticFunctionsOfType(
    crossinline constructorFunction: (T) -> Primitive<T>,
    crossinline plus: (T, T) -> T,
    crossinline minus: (T, T) -> T,
    crossinline mul: (T, T) -> T,
    crossinline div: (T, T) -> T
) =
    mapOf(
        "+" to NativeFunction { self, parameters ->
            val (first, second) = (self!! + parameters).map { it as Primitive<*> }.map { it.innerValue as T }
            constructorFunction(plus(first, second))
        },
        "-" to NativeFunction { self, parameters ->
            val (first, second) = (self!! + parameters).map { it as Primitive<*> }.map { it.innerValue as T }
            constructorFunction(minus(first, second))
        },
        "*" to NativeFunction { self, parameters ->
            val (first, second) = (self!! + parameters).map { it as Primitive<*> }.map { it.innerValue as T }
            constructorFunction(mul(first, second))
        },
        "/" to NativeFunction { self, parameters ->
            val (first, second) = (self!! + parameters).map { it as Primitive<*> }.map { it.innerValue as T }
            constructorFunction(div(first, second))
        }
    )

private operator fun Executor.Object.plus(list: List<Executor.Object>): List<Executor.Object> =
    listOf(this) + list
