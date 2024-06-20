package org.example.util

interface FromString<T> {
    fun fromString(str: String) : T?
}