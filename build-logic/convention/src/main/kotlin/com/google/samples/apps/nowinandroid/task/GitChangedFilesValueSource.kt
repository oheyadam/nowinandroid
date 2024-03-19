package com.google.samples.apps.nowinandroid.task

import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import javax.inject.Inject

abstract class GitChangedFilesValueSource : ValueSource<List<String>, ValueSourceParameters.None> {

    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): List<String> {
        val output = ByteArrayOutputStream()
        execOperations.exec {
            commandLine("git", "diff", "--name-only", "--diff-filter=ACM")
            standardOutput = output
        }
        val result = String(output.toByteArray(), Charset.defaultCharset()).trim().split("\n")
        println("Called from ValueSource! List of changed files: $result")
        return result
    }
}
