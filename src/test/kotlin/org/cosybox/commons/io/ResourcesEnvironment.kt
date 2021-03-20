package org.cosybox.commons.io

import java.io.File
import java.io.IOException
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths

class ResourcesEnvironment {

    companion object {
        const val ROOT_DIRECTORY = "environment"
        private val PATH_SEPARATOR: Char = File.separatorChar
    }

    fun setup() {
        createDirectory(ROOT_DIRECTORY)
    }

    fun cleanup() = Paths.get(ROOT_DIRECTORY).remove()

    fun joinToPath(vararg parts: String): Path = Paths.get(parts.joinToString(PATH_SEPARATOR.toString()))

    fun createResource(path: Path, contents: String? = null): Resource {
        if (exists(path)) {
            throw IOException("\"$path\" exists")
        }

        return Resource(writeString(Paths.get(ROOT_DIRECTORY).resolve(path), contents ?: ""))
    }

    fun createResource(path: String, contents: String? = null): Resource =
        createResource(Paths.get(path), contents)

    fun createResource(vararg parts: String, contents: String? = null): Resource =
        createResource(joinToPath(*parts), contents)

    fun createDirectory(path: Path): Resource {
        if (exists(path)) {
            throw IOException("\"$path exists\"")
        }

        return Resource(createDirectories(Paths.get(ROOT_DIRECTORY).resolve(path)))
    }

    fun createDirectory(path: String): Resource = createDirectory(Paths.get(path))

    fun createDirectory(vararg pats: String): Resource = createDirectory(joinToPath(*pats))

    fun removeResource(path: Path) = path.remove()

    fun removeResource(path: String) = removeResource(Paths.get(path))

    fun removeResource(vararg parts: String) = removeResource(joinToPath(*parts))

    fun resourceAt(path: Path): Resource = Resource(Paths.get(ROOT_DIRECTORY).resolve(path))

    fun resourceAt(path: String): Resource = resourceAt(Paths.get(path))

    fun resourceAt(vararg parts: String): Resource = resourceAt(joinToPath(*parts))

    private fun Path.remove() {
        if (!exists(this)) {
            return
        }

        if (isDirectory(this)) {
            this.list().reversed().forEach {
                delete(it)
            }
        }

        delete(this)
    }

    private fun Path.list(): List<Path> {
        val paths = ArrayList<Path>()

        if (isDirectory(this)) {
            list(this).forEach { path ->
                paths.add(path)

                if (isDirectory(path)) {
                    path.list().forEach {
                        paths.add(it)
                    }
                }
            }
        }

        return paths
    }
}