package org.example.util.commandline

interface CommandStrArgCount {
    val strOpts: List<String>
    val numArgs: Int
}

enum class CommandType : CommandStrArgCount {
    HELP {
        override val strOpts = listOf("-h", "--help")
        override val numArgs: Int = 0
    },
    SUBMISSION_FILE_PATH {
        override val strOpts = listOf("-s")
        override val numArgs: Int = 1
    },
    EXERCISE_SELECTION {
        override val strOpts = listOf("-e")
        override val numArgs : Int = 1
    },
    EXERCISE_CONFIG_FILE_PATH {
        override val strOpts = listOf("-c")
        override val numArgs: Int = 1
    },
    PRETTY_PRINT {
        override val strOpts = listOf("-p")
        override val numArgs: Int = 0
    },
    OUTPUT_DIR_PATH {
        override val strOpts = listOf("-o")
        override val numArgs: Int = 1
    };

    companion object {
        fun toCommandType(s: String): CommandType? {
            for (type in CommandType.entries) {
                for (toString in type.strOpts)
                    if (toString == s)
                        return type
            }
            return null
        }
    }
}