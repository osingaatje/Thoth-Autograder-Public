package org.example.analysis.dynamic_analysis

import org.example.analysis.dynamic_analysis.DynamicTestCaseResult
import org.example.parse.repair.Constants
import org.example.util.compiler.InMemoryJavaCompiler
import java.lang.reflect.Method

/**
 * API for running custom student submissions.
 * @param className name of the class to test
 * @param constructorArgTypes constructor argument types of the class
 *
 * Guide to test:
 * 1. Load Submission (student code)
 * 2. If the method you are going to test is not static, construct a class instance (with the correct types defined in the constructor)
 * 3. Load your method
 * 4. Assert to your heart's content!
 */
class SubmissionRunner(private val className: String, private val constructorArgTypes: Array<out Class<*>>) {
    constructor(className: String) : this(className, arrayOf())

    private var studentClass: Class<*>? = null
    private var classInstance: Any? = null

    private var methodInstance: Method? = null
    private var isMethodStatic: Boolean = false

    /**
     * Tries to compile the student submission and extract the class.
     */
    @Throws(ClassNotFoundException::class)
    fun loadSubmission(submission: String): SubmissionRunner {
//        val name = "${Constants.SUBMISSION_PACKAGE_NAME}.$className"
        val name = className
        this.studentClass = InMemoryJavaCompiler().compile(name, submission)
        return this
    }

    @Throws(InstanceNotLoadedException::class)
    fun constructClassInstance(vararg initArgs: Any): SubmissionRunner {
        if (this.studentClass == null)
            throw InstanceNotLoadedException()

        this.classInstance = this.studentClass!!.getConstructor(*constructorArgTypes)?.newInstance(*initArgs)
            ?: ConstructorNotFoundException(this.studentClass!!.name)
        return this
    }

    /**
     * Run test cases for a method. Each test case is dictated with an input (first element of pair) and expected output (second element of pair)
     */
    @Throws(InstanceNotLoadedException::class)
    fun loadMethod(method: String, isStatic: Boolean, methodSignature: Array<out Class<*>?>): SubmissionRunner {
//        println("loading method $method for class ${this.studentClass} with methodSignature $methodSignature")
        this.methodInstance = this.studentClass?.getMethod(method, *methodSignature)
            ?: throw InstanceNotLoadedException()
        this.isMethodStatic = isStatic
        this.methodInstance?.isAccessible = true;
        return this
    }

    @Throws(MethodNotLoadedException::class, InstanceNotLoadedException::class)
    fun assertEquals(expectedOutput: Any?, vararg inputs: Any?): DynamicTestCaseResult {
        if (!this.isMethodStatic && this.classInstance == null)
            throw InstanceNotLoadedException()

        val actualOutput: Any =
            this.methodInstance?.invoke(if (this.isMethodStatic) null else this.classInstance, *inputs)
                ?: throw MethodNotLoadedException()
        val passed = expectedOutput == actualOutput
        return DynamicTestCaseResult(expectedOutput, actualOutput, passed, inputs)
    }

    /**
     * Asserts multiple test cases at once.
     * @param testCases a list of <expectedOutput, inputs>
     */
    @Throws(MethodNotLoadedException::class, InstanceNotLoadedException::class)
    fun assertEqualsMultiple(testCases: List<Pair<Any, Array<out Any>>>): List<DynamicTestCaseResult> {
        if (this.methodInstance == null)
            throw MethodNotLoadedException()
        if (!this.isMethodStatic && this.classInstance == null)
            throw InstanceNotLoadedException()

        val objectInstance = if (this.isMethodStatic) null else this.classInstance

        val results: MutableList<DynamicTestCaseResult> = mutableListOf()
        for ((expectedOutput, inputs) in testCases) {
            val result = this.methodInstance!!.invoke(objectInstance, *inputs)
            val passed = result.equals(expectedOutput)
            results += DynamicTestCaseResult(expectedOutput, result, passed, inputs)
        }

        return results
    }
}

class InstanceNotLoadedException(msg: String = "Class instance was not loaded before it was required") :
    IllegalArgumentException(msg)

class MethodNotLoadedException(msg: String = "Method was not loaded before it was required") :
    IllegalArgumentException(msg)

class ConstructorNotFoundException(className : String) :
        IllegalArgumentException("Could not find constructor of class $className")