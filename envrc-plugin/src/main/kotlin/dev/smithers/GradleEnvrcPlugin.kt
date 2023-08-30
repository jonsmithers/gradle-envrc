package dev.smithers

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.bufferedReader
import kotlin.io.path.deleteExisting

// 👇 this value can be imported by users in kotlin scripts
lateinit var envrc: ScriptApi

class GradleEnvrcPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(project: org.gradle.api.Project) {
        envrc = ScriptApi()
        // 👇 this value can be referenced by users in groovy scripts
        project.extensions.extraProperties["envrc"] = envrc
    }
}

class ScriptApi {
    private val envrcFile = EnvrcFile()
    operator fun get(key: String): String? {
        return envrcFile.extract(key)
    }
}

private class EnvrcFile {
    private val envrc: File? by lazy { findEnvrc() }
    private var hasPrintedEnvrcOut = false

    fun extract(variableName: String): String? {
        val envrc = envrc ?: return null

        val envrcOut = kotlin.io.path.createTempFile(prefix = "gradle-envrc-out")
        val dollar = "\$"
        val script = """
                if command -v direnv > /dev/null; then
                    if ! direnv status | grep --quiet 'RC allowed true'; then
                        echo -n $SPECIAL_BLOCKED_VALUE
                        exit
                    fi
                    eval "$(direnv stdlib)"
                fi
                source .envrc &> $envrcOut
                if [[ -z "${dollar}{${variableName}+z}" ]]; then
                    echo -n $SPECIAL_NULL_VALUE
                else
                    echo -n ${dollar}{${variableName}}
                fi
                """
        val process = ProcessBuilder()
            .command("bash", "-c", script)
            .directory(envrc.parentFile)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        process.waitFor(60, TimeUnit.SECONDS)
        if (!hasPrintedEnvrcOut) {
            println(envrcOut.bufferedReader().readLines().joinToString("\n") { line -> "source .envrc | $line" })
            hasPrintedEnvrcOut = true
        }
        envrcOut.deleteExisting()
        val result = process.inputStream.bufferedReader().readText()
        if (result == SPECIAL_BLOCKED_VALUE) {
            throw java.lang.IllegalStateException(".envrc is blocked. Run \"direnv allow\".")
        }
        if (result == SPECIAL_NULL_VALUE) {
            return null
        }
        return result
    }
}

private fun findEnvrc(dir: File = File(System.getProperty("user.dir"))): File? {
    if (!dir.isDirectory) {
        throw IllegalStateException("not a directory: $dir")
    }
    val envrc = File("${dir.absolutePath}/.envrc")
    if (envrc.exists()) {
        println("Reading .envrc at ${envrc.absolutePath}")
        return envrc
    }
    if (dir.absolutePath == "/") {
        println("No .envrc found")
        return null
    }
    return findEnvrc(dir.absoluteFile.parentFile)
}

private const val SPECIAL_NULL_VALUE = "special-null-value"
private const val SPECIAL_BLOCKED_VALUE = "special-blocked-value"
