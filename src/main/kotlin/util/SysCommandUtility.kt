package org.example.util

import java.io.File

object SysCommandUtility {
    /**
     * Runs a system-level command in the current context, such as `echo "Hello World"`
     * WARNING: BLocking!
     */
    fun executeSystemLevelCommand(command : String, workDir : File = File(".")) : String {
        val process = ProcessBuilder(*command.split(" ").toTypedArray())
            .directory(workDir)
            .redirectErrorStream(true)
            .start()

        val result = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return result
    }
}