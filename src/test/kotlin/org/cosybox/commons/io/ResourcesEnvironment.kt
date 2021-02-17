package org.cosybox.commons.io

import java.io.File
import java.nio.file.Files
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths

class ResourcesEnvironment {

    companion object {
        const val RESOURCES_DIRECTORY = "test-resources"
        val PATH_SEPARATOR = File.separatorChar
    }

    fun setup() {
        Files.createDirectory(Paths.get(RESOURCES_DIRECTORY))
    }

    fun cleanup() = removeResource(RESOURCES_DIRECTORY)

    fun removeResource(resource: Path) {
        if (!exists(resource)) {
            return
        }

        if (isDirectory(resource)) {
            resource.list().reversed().forEach {
                delete(it)
            }
        }

        delete(resource)
    }

    fun removeResource(resource: String) = removeResource(Paths.get(resource))

    fun removeResource(vararg parts: String) = removeResource(joinToPath(*parts))

    fun createEmptyResource(resource: Path): Path =
        if (exists(resource)) {
            throw RuntimeException("\"$resource\" exists")
        } else {
            writeString(Paths.get(RESOURCES_DIRECTORY).resolve(resource), "")
        }

    fun createEmptyResource(resource: String): Path = createEmptyResource(Paths.get(resource))

    fun createEmptyResource(vararg parts: String): Path = createEmptyResource(joinToPath(*parts))

    fun createResource(resource: Path, contents: String? = null): Path =
        if (contents == null || contents.isEmpty()) {
            createEmptyResource(resource)
        } else {
            writeString(Paths.get(RESOURCES_DIRECTORY).resolve(resource), contents)
        }

    fun createResource(resource: String, contents: String? = null): Path = createResource(Paths.get(resource), contents)

    fun createResource(vararg parts: String, contents: String? = null): Path =
        createResource(joinToPath(*parts), contents)

    fun createDirectory(directory: Path): Path =
        if (exists(directory)) {
            throw RuntimeException("\"$directory\" exists")
        } else {
            createDirectories(Paths.get(RESOURCES_DIRECTORY).resolve(directory))
        }

    fun createDirectory(directory: String): Path = createDirectory(Paths.get(directory))

    fun createDirectory(vararg parts: String): Path = createDirectory(joinToPath(*parts))

    fun joinToPath(vararg parts: String): Path = Paths.get(parts.joinToString(PATH_SEPARATOR.toString()))

    fun resourceAt(path: Path): Path = Paths.get(RESOURCES_DIRECTORY).resolve(path)

    fun resourceAt(path: String): Path = resourceAt(Paths.get(path))

    fun resourceAt(vararg parts: String) = resourceAt(joinToPath(*parts))

    private fun Path.list(): List<Path> {
        val paths = ArrayList<Path>()

        if (isDirectory(this)) {
            list(this).forEach { path ->
                paths.add(path)

                if (isDirectory(path)) {
                    path.list().forEach { paths.add(it) }
                }
            }
        }

        return paths
    }
}