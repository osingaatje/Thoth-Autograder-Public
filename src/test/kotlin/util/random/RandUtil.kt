package util.random

import kotlin.random.Random

object RandUtil {
    fun getRandomString(fromLen : Int = 1, toLen : Int = 10) : String {
        val strColl: StringBuilder = StringBuilder()
        repeat(Random.nextInt(fromLen, toLen)) { strColl.append(Random.nextInt('A'.code, 'z'.code).toChar()) }
        return strColl.toString()
    }
}