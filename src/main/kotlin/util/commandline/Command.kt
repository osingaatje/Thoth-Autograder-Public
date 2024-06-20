package org.example.util.commandline

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

open class Command(val commType: CommandType, val commArgs: List<String>?) {
    init {
        if (commType.numArgs > 0 && commArgs == null || (commArgs != null && commArgs.size != commType.numArgs))
            error("Invalid number of arguments provided for command ${commType.name}: expected ${commType.numArgs}, got ${commArgs?.size ?: 0} (args=$commArgs)")
    }
    override fun toString(): String {
        return "$commType: $commArgs"
    }

    companion object {
        private fun getCommandClass(commType: CommandType) : KClass<out Command> {
            return when(commType) {
                CommandType.HELP -> HelpCommand::class
                CommandType.SUBMISSION_FILE_PATH -> InputFileCommand::class
                CommandType.EXERCISE_SELECTION -> ExerciseSelectionCommand::class
                CommandType.PRETTY_PRINT -> PrettyPrintCommand::class
                CommandType.EXERCISE_CONFIG_FILE_PATH -> ExerciseConfigCommand::class
                CommandType.OUTPUT_DIR_PATH -> OutputDirectoryPathCommand::class
            }
        }

        /**
         * Greedily parses the command list. No verification! Do that yourself :)
         */
        fun convertToCommandMap(arr: Array<String>): Map<CommandType, Command> {
            val result : MutableMap<CommandType, Command> = mutableMapOf()

            var index = 0
            while (index < arr.size) {
                val cType: CommandType = CommandType.toCommandType(arr[index]) ?: error("No command found with value ${arr[index]}")
                val args : List<String>? = if (cType.numArgs > 0) arr.slice(IntRange(index + 1, index + cType.numArgs)) else null

                result[cType] = getCommandClass(cType).primaryConstructor?.call(args) ?: error("Could not construct command $cType with args $args")

                index += 1 + cType.numArgs
            }

            return result
        }
    }
}

class HelpCommand(args : List<String>?) : Command(CommandType.HELP, null)
class InputFileCommand(pathList : List<String>) : Command(CommandType.SUBMISSION_FILE_PATH, pathList) {
    fun getPath() : String = this.commArgs?.get(0) ?: error("InputFileCommand should have one argument (path)!")
}
class ExerciseSelectionCommand(selectionList : List<String>) : Command(CommandType.EXERCISE_SELECTION, selectionList) {
    fun getSelection() : Int {
        try {
            return this.commArgs?.get(0)?.toInt() ?: error("Exercise Selection needs an exercise number!")
        } catch (e : NumberFormatException) {
            error("Exercise number must be an integer")
        }
    }
}
class ExerciseConfigCommand(pathList: List<String>) : Command(CommandType.EXERCISE_CONFIG_FILE_PATH, pathList) {
    fun getPath() : String = this.commArgs?.get(0) ?: error("ExerciseConfigCommand should have at least one argument (path)!")
}
class PrettyPrintCommand(args : List<String>?) : Command(CommandType.PRETTY_PRINT, null)
class OutputDirectoryPathCommand(pathList : List<String>) : Command(CommandType.OUTPUT_DIR_PATH, pathList) {
    fun getPath() : String = this.commArgs?.get(0) ?: error("OutputDirectoryPathCommand should have at least one argument (path)!")
}