package org.cosybox.commons.io

import org.cosybox.commons.io.prerequisites.ResourcePrerequisites.resourceExists
import org.cosybox.commons.io.prerequisites.ResourcePrerequisites.resourceIsAbsent
import org.cosybox.commons.io.prerequisites.ResourcePrerequisites.resourceIsDirectory
import org.cosybox.commons.io.prerequisites.ResourcePrerequisites.resourceParentExists
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Resource(val path: Path) {

    companion object {
        private val PATH_SEPARATOR: Char = File.separatorChar
        private const val EXTENSION_SEPARATOR: Char = '.'

        fun createFromInputStream(inputStream: InputStream, path: Path): Resource {
            val resource = Resource(path)

            resourceIsAbsent(resource)
            resourceParentExists(resource)
            resourceIsDirectory(resource.parent!!)

            return inputStream.copyTo(resource)
        }

        fun createFromInputStream(inputStream: InputStream, path: String): Resource =
            createFromInputStream(inputStream, Paths.get(path))

        private fun InputStream.copyTo(resource: Resource): Resource =
            resource.path.let {
                Files.copy(this, it)
                resource
            }
    }

    val name: String
    val extension: String?
    val parent: Resource?

    constructor(path: String) : this(Paths.get(path))

    init {
        name = path.toString()
            .removeSuffix(PATH_SEPARATOR.toString())
            .substringAfterLast(PATH_SEPARATOR)
        extension = name.lastIndexOf(EXTENSION_SEPARATOR).let {
            if (it > 0) {
                name.substring(it + 1)
            } else {
                null
            }
        }
        parent = path.parent?.let { Resource(it) }
    }

    fun exists(): Boolean = Files.exists(path)

    fun isDirectory(): Boolean {
        resourceExists(this)
        return Files.isDirectory(path)
    }

    fun list(depth: Int = 1): List<Resource> {
        resourceExists(this)
        resourceIsDirectory(this)

        if (depth < 1) {
            return emptyList()
        }

        val resources = ArrayList<Resource>()

        Files.list(path).map { Resource(it) }.forEach { resource ->
            resources.add(resource)

            if (resource.isDirectory()) {
                resource.list(depth.dec()).forEach { resources.add(it) }
            }
        }

        return resources
    }

    fun isEmpty(): Boolean {
        resourceExists(this)
        resourceIsDirectory(this)
        return list().isEmpty()
    }

    fun bytesCount(): Long =
        if (isDirectory()) {
            list(Int.MAX_VALUE)
                .map { if (it.isDirectory()) 0L else it.bytesCount() }
                .sum()
        } else {
            Files.size(path)
        }

    fun remove() {
        if (isDirectory()) {
            clearDirectory(this)
        }

        Files.delete(path)
    }

    fun removeContents() {
        resourceExists(this)
        resourceIsDirectory(this)
        clearDirectory(this)
    }

    fun copyTo(directory: Resource): Resource {
        resourceExists(this)
        resourceExists(directory)
        resourceIsDirectory(directory)

        val resource = Resource(directory.path.resolve(name))

        resourceIsAbsent(resource)
        return copy(this, resource)
    }

    fun copyTo(directory: Path): Resource = copyTo(Resource(directory))

    fun copyTo(directory: String): Resource = copyTo(Resource(directory))

    fun copyAs(resource: Resource): Resource {
        resourceExists(this)
        resourceIsAbsent(resource)
        resourceParentExists(resource)
        resourceIsDirectory(resource.parent!!)
        return copy(this, resource)
    }

    fun copyAs(resource: Path): Resource = copyAs(Resource(resource))

    fun copyAs(resource: String): Resource = copyAs(Resource(resource))

    fun renameTo(name: String): Resource {
        require(!name.trim().isEmpty()) {
            "Argument \"name\" cannot be empty"
        }

        resourceExists(this)
        val resource = Resource(path.resolveSibling(name))
        resourceIsAbsent(resource)
        Files.move(path, resource.path)
        return resource
    }

    fun moveTo(directory: Resource): Resource {
        val resource = copyTo(directory)
        remove()
        return resource
    }

    fun moveTo(directory: Path): Resource = moveTo(Resource(directory))

    fun moveTo(directory: String): Resource = moveTo(Resource(directory))

    fun openInputStream(): InputStream = Files.newInputStream(path)

    fun openOutputStream(): OutputStream = Files.newOutputStream(path)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Resource

        if (path != other.path) return false
        if (name != other.name) return false
        if (extension != other.extension) return false
        if (parent != other.parent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (extension?.hashCode() ?: 0)
        result = 31 * result + (parent?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = path.toString()

    private fun clearDirectory(directory: Resource) = directory.list(Int.MAX_VALUE)
        .reversed()
        .forEach { Files.delete(it.path) }

    private fun copy(source: Resource, target: Resource): Resource {
        Files.copy(source.path, target.path)

        if (source.isDirectory()) {
            source.list(Int.MAX_VALUE)
                .forEach { Files.copy(it.path, target.path.resolve(source.path.relativize(it.path))) }
        }

        return target
    }
}