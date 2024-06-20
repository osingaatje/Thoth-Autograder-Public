package org.example.grading.scheme.vars
import org.example.util.FromString

//-------------------------------------------- ENUMS --------------------------------------------//

enum class PrimitiveType {
    BOOL, NUM  /* byte, int, long etc. */, DECIMAL  /* float, double, etc. */, STR   /* char, string, char*, etc. */, ANY /* private item for deserialization stuff */;

    override fun toString(): String {
        return when (this) {
            BOOL -> "BOOL"
            NUM -> "NUM"
            DECIMAL -> "DEC"
            STR -> "STR"
            ANY -> "?"
        }
    }

    companion object : FromString<PrimitiveType> {
        override fun fromString(str: String): PrimitiveType? {
            return when (str) {
                "BOOL" -> BOOL
                "NUM" -> NUM
                "DEC" -> DECIMAL
                "STR" -> STR
                else -> null // you cannot convert ANY from a string back. this is reserved for serialisation
            }
        }
    }
}

enum class ComplexType {
    LIST {
        override fun numArgTypes(): Int = 1
        override fun childrenCount(): Int = -1 // arbitrary
    },
    MAP {
        override fun numArgTypes(): Int  = 2
        override fun childrenCount(): Int = 2 // must have two children (two lists)
    };

    abstract fun numArgTypes(): Int
    abstract fun childrenCount() : Int


    override fun toString(): String {
        return when (this) {
            LIST -> "LIST"
            MAP -> "MAP"
        }
    }

    companion object : FromString<ComplexType> {
        override fun fromString(str: String): ComplexType? {
            return when (str) {
                "LIST" -> LIST
                "MAP" -> MAP
                else -> null
            }
        }
    }
}


//-------------------------------------------- CLASSES --------------------------------------------//
/**
 * Generic adaptor for primitive and complex data types
 */
abstract class DataVar {
    abstract fun getClassRepresentation() : Class<*>?
    abstract fun toTreeString(): String
}

/**
 * Represents a primitive data type
 */
class PrimitiveVar(val type: PrimitiveType) : DataVar() {
    override fun toString(): String {
        return type.toString()
    }

    override fun getClassRepresentation(): Class<*> {
        return when(type) {
            PrimitiveType.BOOL -> Boolean::class.java
            PrimitiveType.NUM -> Int::class.java
            PrimitiveType.DECIMAL -> Double::class.java
            PrimitiveType.STR -> String::class.java
            PrimitiveType.ANY -> error("Any should not be represented in a final datatype!")
        }
    }

    override fun toTreeString(): String {
        return this.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is PrimitiveVar) return false
        if (this.type == PrimitiveType.ANY || other.type == PrimitiveType.ANY) return true // ANY can be anything, so match anything.
        return this.type == other.type
    }

    // auto-generated, no idea if good
    override fun hashCode(): Int {
        return 31 * type.hashCode()
    }
}

/**
 * Represents a complex data type.
 * Examples: LIST<INT>, MAP<INT, LIST<BOOL>>
 */
class ComplexVar(val type: ComplexType, val args: List<DataVar>) : DataVar() {
    init {
        if (type.numArgTypes() != -1)
            require(args.size == type.numArgTypes()) { "Invalid number of arguments for $type: expected ${type.numArgTypes()}, got ${args.size}" }
    }

    override fun toString(): String {
        return "$type<${args.joinToString(", ")}>"
    }

    override fun getClassRepresentation(): Class<*> {
        return when(type) {
            ComplexType.LIST -> ArrayList::class.java
            ComplexType.MAP -> HashMap::class.java
        }
    }

    override fun toTreeString(): String {
        return "$type" // no <args> here, because these will be encoded in the children
    }

    override fun equals(other: Any?): Boolean {
        return other is ComplexVar && other.type == type && other.args == args
    }

    // auto-generated, no idea if good
    override fun hashCode(): Int {
        var result = type.hashCode()
        val argsRes = args.fold(0) { sum, element -> sum + element.hashCode() }
        result = 31 * result + argsRes
        return result
    }
}

class NullVar : DataVar() {
    override fun getClassRepresentation(): Class<*>? {
        return null
    }

    override fun toTreeString(): String {
        return "NULL"
    }

    override fun toString(): String {
        return "NULL"
    }

    override fun hashCode(): Int {
        return 696969420 // some random high number
    }

    override fun equals(other: Any?): Boolean {
        return other is NullVar
    }
}

//-------------------------------------------- SERIALIZABLE STUFF --------------------------------------------//
// Must be implemented for all possible types of DataType (e.g. Map, List, Int, etc.)

/**
 * Denotes the Node in a tree (that has children).
 * ### PLEASE NOTE: A MAP MUST ALWAYS HAVE TWO LISTS (a keys and values list)
 */
open class ValNode(val dt: DataVar, val children: List<ValNode>) {
    init {
        if (dt is ComplexVar && dt.type == ComplexType.MAP
            && (children.size != 2 || !children.all { it.dt is ComplexVar && it.dt.type == ComplexType.LIST && it.children.size == children[0].children.size })) {
            error("A \"MAP\" complex var must always have two children lists (a 'keys' and 'values' list) which must be exactly equal! \n" +
                    "$dt: $children\n" +
                    "ChildListTypes:${dt.args.getOrNull(0)} -> ${dt.args.getOrNull(1)}\n" +
                    "Child Sizes: ${children.getOrNull(0)?.children?.size} and ${children.getOrNull(1)?.children?.size}")
        }
    }

    override fun toString(): String {
        return "${dt.toTreeString()}[${children.joinToString(",")}]"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ValNode) return false
        return this.dt == other.dt && this.children == other.children
    }

    override fun hashCode(): Int {
        var result = dt.hashCode()
        result = 31 * result + children.hashCode()
        return result
    }

    open fun getKotlinValue(): Any? {
        val childValues : List<Any?> = children.map { it.getKotlinValue() }
        return when((this.dt as ComplexVar).type) {
            ComplexType.MAP -> {
                val keys = childValues[0] as List<*> // assuming that the map is a valid map
                val values = childValues[1] as List<*> // idem

                HashMap(keys.mapIndexed { i, key -> key to values[i] }.toMap())
            }
            ComplexType.LIST -> {
                return listOf(*childValues.toTypedArray())
            }
        }
    }
}

class ValLeaf(val type: PrimitiveVar, val value: Any) : ValNode(type, listOf()) {
    override fun toString(): String {
        val typeStr = type.toString()
        val value = value.toString()
            .replace("[\\\\\\[\\]()]".toRegex()) { res -> "\\${res.value}" } // escape special characters in strings (somehow another backslash is added by the encoder, don't worry about it)
        return "$typeStr($value)"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ValLeaf) return false
        return this.type == other.type && this.value == other.value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun getKotlinValue() : Any {
        return this.value
    }
}

class ValNull : ValNode(NullVar(), listOf()) {
    override fun toString(): String {
        return "NULL"
    }

    override fun getKotlinValue(): Any? {
        return null
    }
}

fun main() {
    val bool1 = ValLeaf(PrimitiveVar(PrimitiveType.BOOL), true)
    val bool2 = ValLeaf(PrimitiveVar(PrimitiveType.BOOL), false)


    val listNode = ValNode(ComplexVar(ComplexType.LIST, listOf(PrimitiveVar(PrimitiveType.BOOL))), listOf(bool1, bool2))

    println(listNode)
}