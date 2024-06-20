package org.example.util.serialisation

interface CustomSerializer<T> {
    fun serialize(obj : T) : String
    fun deserialize(s : String) : T
}
