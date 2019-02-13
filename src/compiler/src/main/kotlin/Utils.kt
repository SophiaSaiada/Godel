package com.godel.compiler

fun <T> Iterator<T>.nextOrNull() = if (hasNext()) next() else null
