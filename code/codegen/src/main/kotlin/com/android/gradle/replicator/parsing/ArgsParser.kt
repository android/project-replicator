package com.android.gradle.replicator.parsing

import java.io.File
import java.io.FileReader
import java.util.*

/**
 * Class for parsing command line and property file arguments
 */
class ArgsParser {
    companion object {
        const val UNLIMITED_ARGC = -1
    }
    class Option(val argc: Int) {
        val argv = mutableListOf<String>()
        var isPresent: Boolean = false
        val first
            get() = argv[0]
        val orNull
            get() = if (isPresent) this else null
    }

    private val longNameOpts = mutableMapOf<String, Option>()
    private val shortNameOpts = mutableMapOf<String, Option>()
    private val argsFileOpts = mutableMapOf<String, Option>()

    /**
     * Creates an [Option], stores it and returns it
     *
     * @param longName: the long name for the option (ex: option for --option)
     * @param shortName: the short name for the option (ex: o for -o)
     * @param propertyName: the property file name for the option (ex: option for option=...)
     * @return: the created option
     */
    fun option(longName: String = "", shortName: String = "", propertyName: String = "", argc: Int = 0): Option {
        val option = Option(argc)

        if (longName.isNotEmpty()) {
            if (longNameOpts.containsKey("--$longName")) {
                throw IllegalArgumentException("arg $longName already exists")
            }
            longNameOpts["--$longName"] = option
        }
        if (shortName.isNotEmpty()) {
            if (shortNameOpts.containsKey("-$shortName")) {
                throw IllegalArgumentException("arg $shortName already exists")
            }
            shortNameOpts["-$shortName"] = option
        }
        if (propertyName.isNotEmpty()) {
            if (argsFileOpts.containsKey(propertyName)) {
                throw IllegalArgumentException("arg $propertyName already exists")
            }
            argsFileOpts[propertyName] = option
        }
        if (shortName.isEmpty() && longName.isEmpty() && propertyName.isEmpty()) {
            throw IllegalArgumentException("option must have a short name, a long name or an args file name")
        }

        return option
    }

    /**
     * Parses the command-line args and populates the previously created options
     *
     * @param args: the command-line args
     */
    fun parseArgs(args: Array<String>) {
        var lastOption: Option? = null

        args.forEach { arg ->
            if (arg.startsWith("--")) {
                // Sanity check
                if (!longNameOpts.containsKey(arg)) {
                    throw java.lang.IllegalArgumentException("invalid argument $arg")
                }
                if (longNameOpts[arg]!!.isPresent) {
                    throw java.lang.IllegalArgumentException("option used twice $arg")
                }
                lastOption = longNameOpts[arg]!!
                lastOption!!.isPresent = true
            } else if (arg.startsWith("-") && arg.toIntOrNull() == null) {
                // Sanity check
                if (!shortNameOpts.containsKey(arg)) {
                    throw java.lang.IllegalArgumentException("invalid argument $arg")
                }
                if (shortNameOpts[arg]!!.isPresent) {
                    throw java.lang.IllegalArgumentException("option used twice $arg")
                }
                lastOption = shortNameOpts[arg]!!
                lastOption!!.isPresent = true
            } else {
                lastOption!!.argv.add(arg)
            }
        }
        // Post-parse sanity check
        (shortNameOpts + longNameOpts).forEach { opt ->
            if (opt.value.isPresent && opt.value.argv.size != UNLIMITED_ARGC && opt.value.argv.size != opt.value.argc) {
                throw java.lang.IllegalArgumentException("wrong # of args for ${opt.key}. Expected ${opt.value.argc} but got ${opt.value.argv.size}")
            }
        }
    }

    /**
     * Parses a property file and populates the previously created options
     *
     * @param propertyFile: the property file
     */
    fun parsePropertyFile(propertyFile: File) {
        val arguments = FileReader(propertyFile).use {
            Properties().also { properties -> properties.load(it) }
        }

        arguments.forEach { arg ->
            // Ignore invalid args
            if (argsFileOpts.containsKey(arg.key)) {
                val currentOption = argsFileOpts[arg.key]!!

                // Override command line options
                if (currentOption.isPresent) {
                    currentOption.argv.clear()
                }
                arg.value?.toString()?.split(",")?.let {
                    currentOption.argv.addAll(it)
                }
            }
        }
        // Post-parse sanity check
        argsFileOpts.forEach { opt ->
            if (opt.value.isPresent && opt.value.argv.size != UNLIMITED_ARGC && opt.value.argv.size != opt.value.argc) {
                throw java.lang.IllegalArgumentException("wrong # of args for ${opt.key}. Expected ${opt.value.argc} but got ${opt.value.argv.size}")
            }
        }
    }
}