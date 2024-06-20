package org.example.grading.scheme.vars

import org.example.util.serialisation.CustomSerializer

//==========================================================================================================================
//
// WARNING: DO NOT USE THE INCLUDED KSerializer.serialize(), this will add extra backslashes and shit!!! Cost me 3 hours...
//
//==========================================================================================================================

fun getFirstDimensionalArgs(
    s: String, openingChar: Char, closingChar: Char, separator: Char = ','
): List<String> {
    val args: MutableList<String> = mutableListOf()
    // count '<' and '>' to be able to split the arguments on the first level (to prevent 3D structures and deeper from being cut up)
    var count = 0
    var collector = ""

    var index = -1
    while (index < s.length - 1) {
        index++
        val c = s[index]

        if (c == '\\') {
            collector += s[index]
            collector += s[index + 1] // add other character regardless of what it is
            index++
        } else if (c == openingChar) {
            collector += c
            count++
        }
        else if (c == closingChar) {
            collector += c
            count--
        }
        else if (c == separator && count == 0) {
            args.add(collector)
            collector = ""
        } else {
            collector += c
        }
    }
    if (collector.isNotEmpty()) args.add(collector) // add the remaining string

    return args
}


/**
 * For encoding / decoding primitive (int, bool) or complex (List<?>, Map<?>) data types
 */
object DataVarSerializer : CustomSerializer<DataVar> {
    override fun serialize(obj: DataVar): String {
        when (obj) {
            is PrimitiveVar -> return obj.type.toString()
            is ComplexVar -> {
                if (obj.args.size != obj.type.numArgTypes()) {
                    throw IllegalArgumentException("${obj.type} type must have exactly ${obj.type.numArgTypes()} argument(s)! (got ${obj.args.size})")
                }
                return "${obj.type}<${obj.args.joinToString(",") { serialize(it) }}>"
            }
            is NullVar -> return "NULL"
        }
        throw IllegalArgumentException("Invalid data type: $obj")
    }

    @kotlin.jvm.Throws(IllegalArgumentException::class)
    override fun deserialize(s: String): DataVar {
        // null case:
        if (s == "NULL")
            return NullVar()

        // primitive case
        val primType = PrimitiveType.fromString(s)
        if (primType != null)
            return PrimitiveVar(primType)

        if (s.indexOf('<') == -1 || s.indexOf('>') == -1) throw IllegalArgumentException("Invalid complex data type (missing '<' or '>'): $s")

        // complex case:
        val type = s.substringBefore('<')
        val argStr = s.substringAfter('<')
            .substringBeforeLast('>') // split up the "MAP<...,...>" for example into "...,..." (everything within brackets)

        var args = getFirstDimensionalArgs(argStr, '<', '>')

        // strip whitespace: (to prevent " BOOL" etc. from not being a valid primitive type in structures such as "MAP<INT, BOOL>")
        args = args.map { it.trim() }.toMutableList()

        val argTypes = args.map { deserialize(it) } // recursively deserialize the arguments
        return when (type) {
            ComplexType.LIST.toString() -> ComplexVar(ComplexType.LIST, argTypes)
            ComplexType.MAP.toString() -> ComplexVar(ComplexType.MAP, argTypes)
            else -> throw IllegalArgumentException("Invalid data type: $s")
        }
    }
}

object PrimitiveVarSerializer : CustomSerializer<PrimitiveVar> {
    override fun deserialize(s: String): PrimitiveVar {
        return PrimitiveType.fromString(s.trim())?.let { PrimitiveVar(it) } ?: run {
            error("PrimitiveVarSerializer can only deserialize PrimitiveVar (string=$s)")
        }
    }

    override fun serialize(obj: PrimitiveVar): String {
        return obj.type.toString()
    }
}

object ComplexVarSerializer : CustomSerializer<ComplexVar> {
    override fun deserialize(s: String): ComplexVar {
        return when (val result = DataVarSerializer.deserialize(s)) {
            is ComplexVar -> result
            else -> throw IllegalArgumentException("ComplexVarSerializer can only deserialize ComplexVar")
        }
    }

    override fun serialize(obj: ComplexVar): String {
        return DataVarSerializer.serialize(obj)
    }
}


object ValNodeSerializer : CustomSerializer<ValNode> {
    /**
     * Decodes structure: TYPE(VAL)
     */
    @Throws(IllegalStateException::class)
    private fun decodePrimitive(s: String): ValLeaf {
        val type: String = s.substringBefore('(')
        val value: String = s.substringAfter('(').substringBeforeLast(')')

        val varType: PrimitiveVar = PrimitiveVarSerializer.deserialize(type)
        try {
            val varValue = when (varType.type) {
                PrimitiveType.BOOL -> value.toBooleanStrict()
                PrimitiveType.NUM -> value.toInt()
                PrimitiveType.DECIMAL -> value.toDouble()
                PrimitiveType.STR -> {
                    // escape single ('\' or double '\\' quotes) (because somehow the encoder adds more??)
                    value.replace("\\\\(.)".toRegex()) { res ->
                        res.groups.last()?.value ?: error("Escape regex is brok!")
                    } // skip the '\' character
                }

                PrimitiveType.ANY -> error("Should not be able to decode an ANY.")
            }
            return ValLeaf(varType, varValue)
        } catch (e: Exception) { // NumberFormatException, IllegalArgumentException, etc.
            error("Could not construct a valid $varType out of value $value")
        }
    }

    /**
     * Decodes structure: TYPE[subnode1, subnode2, ...]
     */
    private fun decodeComplex(s: String): ValNode {
        val typeStr = s.substringBefore('[')
        val valueStr = s.substringAfter('[').substringBeforeLast(']')
        val args = getFirstDimensionalArgs(valueStr, '[', ']', ',')

        val complexType: ComplexType = ComplexType.fromString(typeStr.trim()) ?: error("Invalid complex type: $typeStr")

        // if we have a fixed arg count, check that:
        if (complexType.childrenCount() != -1 && args.size != complexType.numArgTypes())
            error("Number of arguments not correct for complex type $complexType. Expected ${complexType.numArgTypes()}, got ${args.size}")

        // deserialize args
        val argNodes: List<ValNode> = args.map { deserialize(it) }

        // check if the types of args match:
        if (args.isNotEmpty())
            argNodes.all { it.dt == argNodes[0].dt }

        // make sure that types such as "LIST" have **datatype** LIST[DEC] but that the **value type** is LIST[DEC(..), DEC(...), ...]
        val complexTypeChildren : List<DataVar>
            = if (complexType.childrenCount() == -1 && args.isNotEmpty())
                listOf(argNodes[0].dt)
            else if (argNodes.isNotEmpty()) argNodes.map { it.dt } else listOf(PrimitiveVar(PrimitiveType.ANY))

        if (complexType == ComplexType.MAP) {
            // ignore the nested LISTs in the datatype of map.
            if (complexTypeChildren.size != 2 || !complexTypeChildren.all { it is ComplexVar && it.type == ComplexType.LIST && it.args.size == 1 } )
                error("Error deserializing MAP var: child types were not size 2 or lists (for ValNodes, a MAP<?,?> is represented by a MAP<LIST<?>, LIST<?>> value type to accommodate lists of key/value pairs\n\t$complexTypeChildren")

            val ignoredListChildrenTypes = complexTypeChildren.map { (it as ComplexVar).args[0] }
            return ValNode(ComplexVar(complexType, ignoredListChildrenTypes), argNodes)
        } else {
            return ValNode(ComplexVar(complexType, complexTypeChildren), argNodes)
        }
    }

    override fun deserialize(s: String): ValNode {
        if (s == "NULL")
            return ValNull()

        val indexOfBrace = s.indexOf('[')
        val indexOfParens = s.indexOf('(')
        if (indexOfBrace != -1 && (indexOfParens == -1 || indexOfBrace < indexOfParens)) {
            return decodeComplex(s)
        }
        return decodePrimitive(s)
    }

    override fun serialize(obj: ValNode): String {
        return obj.toString()
    }
}