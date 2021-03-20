package org.cosybox.commons.io

import org.cosybox.commons.io.prerequisites.ResourceRequirement.*
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import org.cosybox.commons.io.prerequisites.ResourcePrerequisites.require as require

class Resource(val path: Path) {

    companion object {
        val PATH_SEPARATOR: Char = File.separatorChar
        private const val EXTENSION_SEPARATOR: Char = '.'

        fun createFromInputStream(inputStream: InputStream, path: Path): Resource {
            val resource = Resource(path)
            require(RESOURCE_DOES_NOT_EXIST, resource)
            require(RESOURCE_PARENT_EXIST, resource)
            require(RESOURCE_IS_DIRECTORY, resource.parent!!)
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

    val name: String = path
        .toString()
        .split(PATH_SEPARATOR)
        .last()
    val extension: String? = name
        .lastIndexOf(EXTENSION_SEPARATOR)
        .let { if (it > 0) name.substring(it + 1) else null }
    val parent: Resource? = path.parent?.let { Resource(it) }

    constructor(path: String) : this(Paths.get(path)) {
        if (path.isEmpty()) {
            throw InstantiationException("Argument \"path\" cannot be empty")
        }
    }

    fun exists(): Boolean = Files.exists(path)

    fun isDirectory(): Boolean {
        require(RESOURCE_EXISTS, this)
        return Files.isDirectory(path)
    }

    fun list(depth: Int = 1): List<Resource> {
        require(RESOURCE_EXISTS, this)
        require(RESOURCE_IS_DIRECTORY, this)

        if (depth < 1) {
            return emptyList()
        }

        val resources = ArrayList<Resource>()

        listContents(this).forEach { resource ->
            resources.add(resource)

            if (resource.isDirectory()) {
                resource.list(depth.dec()).forEach {
                    resources.add(it)
                }
            }
        }

        return resources
    }

    fun isEmpty(): Boolean {
        require(RESOURCE_EXISTS, this)
        require(RESOURCE_IS_DIRECTORY, this)
        return listContents(this).isEmpty()
    }

    fun bytesCount(): Long =
        if (isDirectory()) {
            list(Int.MAX_VALUE)
                .map { if (it.isDirectory()) 0 else it.bytesCount() }
                .sum()
        } else {
            Files.size(path)
        }

    fun remove() {
        if (isDirectory()) {
            removeContents(this)
        }

        Files.delete(path)
    }

    fun removeContents() {
        require(RESOURCE_EXISTS, this)
        require(RESOURCE_IS_DIRECTORY, this)
        removeContents(this)
    }

    fun copyTo(directory: Resource): Resource {
        require(RESOURCE_EXISTS, this)
        require(RESOURCE_EXISTS, directory)
        require(RESOURCE_IS_DIRECTORY, directory)
        val resource = Resource(directory.path.resolve(name))
        require(RESOURCE_DOES_NOT_EXIST, resource)
        return copy(this, resource)
    }

    fun copyTo(directory: Path): Resource = copyTo(Resource(directory))

    fun copyTo(directory: String): Resource = copyTo(Resource(directory))

    fun copyAs(resource: Resource): Resource {
        require(RESOURCE_EXISTS, this)
        require(RESOURCE_DOES_NOT_EXIST, resource)
        require(RESOURCE_PARENT_EXIST, resource)
        require(RESOURCE_IS_DIRECTORY, resource.parent!!)
        return copy(this, resource)
    }

    fun copyAs(resource: Path): Resource = copyAs(Resource(resource))

    fun copyAs(resource: String): Resource = copyAs(Resource(resource))

    fun renameTo(name: String): Resource {
        require(!name.trim().isEmpty()) {
            "Argument \"name\" cannot be empty"
        }

        require(RESOURCE_EXISTS, this)
        val resource = Resource(path.resolveSibling(name))
        require(RESOURCE_DOES_NOT_EXIST, resource)
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

    fun createDirectory(): Resource {
        require(RESOURCE_DOES_NOT_EXIST, this)
        require(RESOURCE_PARENT_EXIST, parent!!)
        require(RESOURCE_IS_DIRECTORY, parent)
        return Resource(Files.createDirectory(path))
    }

    fun createDirectories(): Resource {
        require(RESOURCE_DOES_NOT_EXIST, this)
        return Resource(Files.createDirectories(path))
    }

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

    private fun listContents(directory: Resource): List<Resource> =
        Files.list(directory.path).map { Resource(it) }.collect(Collectors.toList())

    private fun removeContents(directory: Resource) =
        directory.list(Int.MAX_VALUE).reversed().forEach { Files.delete(it.path) }

    private fun copy(resource: Resource, destination: Resource): Resource {
        Files.copy(resource.path, destination.path)

        if (resource.isDirectory()) {
            resource.list(Int.MAX_VALUE).forEach {
                Files.copy(it.path, destination.path.resolve(resource.path.relativize(it.path)))
            }
        }

        return destination
    }
}