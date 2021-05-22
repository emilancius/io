package org.cosybox.commons.io

import org.cosybox.commons.io.compression.CompressionParameters
import org.cosybox.commons.io.compression.CompressionType
import org.cosybox.commons.io.compression.CompressorFactory
import org.cosybox.commons.io.compression.ExtractorFactory
import org.cosybox.commons.io.exceptions.ResourceException
import java.nio.file.Path
import org.cosybox.commons.io.prerequisites.ResourceRequirements.Companion.require
import org.cosybox.commons.io.prerequisites.ResourceRequirements.Requirement.*
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import kotlin.io.path.*

class Resource(val path: Path) {

    companion object {
        val PATH_SEPARATOR: Char = File.separatorChar
        private const val EXTENSION_SEPARATOR: Char = '.'

        fun createFromInputStream(inputStream: InputStream, path: Path): Resource {
            val resource = Resource(path)
            require(RESOURCE_DOES_NOT_EXIST, resource)
            require(PARENT_DIRECTORY_EXISTS, resource)
            return inputStream.copyTo(resource)
        }

        fun createFromInputStream(inputStream: InputStream, path: String): Resource =
            createFromInputStream(inputStream, Path.of(path))

        private fun InputStream.copyTo(resource: Resource): Resource =
            resource.path.let {
                Files.copy(this, it)
                resource
            }
    }

    val name: String = path.name
    val extension: String? = name
        .lastIndexOf(EXTENSION_SEPARATOR)
        .let { if (it > 0) name.substring(it + 1) else null }
    val parent: Resource? = path.parent?.let { Resource(it) }

    constructor(path: String) : this(Path.of(path)) {
        if (path.isEmpty()) {
            throw InstantiationException("Argument \"path\" cannot be empty")
        }
    }

    fun exists(): Boolean = path.exists()

    fun isDirectory(): Boolean {
        require(RESOURCE_EXISTS, this)
        return path.isDirectory()
    }

    fun list(depth: Long = 1): List<Resource> {
        require(RESOURCE_EXISTS, this)
        require(RESOURCE_IS_DIRECTORY, this)

        if (depth < 1) {
            return emptyList()
        }

        val resources = ArrayList<Resource>()

        list(this).forEach { resource ->
            resources.add(resource)

            if (resource.isDirectory()) {
                resource.list(depth = depth.dec()).forEach { resources.add(it) }
            }
        }
        return resources
    }

    fun isEmpty(): Boolean {
        require(RESOURCE_EXISTS, this)
        require(RESOURCE_IS_DIRECTORY, this)
        return list(this).isEmpty()
    }

    fun bytesCount(): Long =
        if (isDirectory()) {
            list(Long.MAX_VALUE).sumOf { if (it.isDirectory()) 0L else it.bytesCount() }
        } else {
            Files.size(path)
        }

    fun removeContents() {
        require(RESOURCE_EXISTS, this)
        require(RESOURCE_IS_DIRECTORY, this)
        removeContents(this)
    }

    fun remove() {
        if (isDirectory()) {
            removeContents()
        }

        Files.delete(this.path)
    }

    fun copyTo(directory: Resource, copyStrategy: CopyStrategy = CopyStrategy.RAISE_EXCEPTION_ON_CONFLICT): Resource {
        require(RESOURCE_EXISTS, this)
        require(RESOURCE_EXISTS, directory)
        require(RESOURCE_IS_DIRECTORY, directory)
        val copy = Resource(directory.path.resolve(name))
        return copy(this, copy, copyStrategy)
    }

    fun copyTo(directory: Path, copyStrategy: CopyStrategy = CopyStrategy.RAISE_EXCEPTION_ON_CONFLICT): Resource =
        copyTo(Resource(directory), copyStrategy)

    fun copyTo(directory: String, copyStrategy: CopyStrategy = CopyStrategy.RAISE_EXCEPTION_ON_CONFLICT): Resource =
        copyTo(Resource(directory), copyStrategy)

    fun copyAs(resource: Resource, copyStrategy: CopyStrategy = CopyStrategy.RAISE_EXCEPTION_ON_CONFLICT): Resource {
        require(RESOURCE_EXISTS, this)
        require(PARENT_DIRECTORY_EXISTS, resource)
        return copy(this, resource, copyStrategy)
    }

    fun copyAs(resource: Path, copyStrategy: CopyStrategy = CopyStrategy.RAISE_EXCEPTION_ON_CONFLICT): Resource =
        copyAs(Resource(resource), copyStrategy)

    fun copyAs(resource: String, copyStrategy: CopyStrategy = CopyStrategy.RAISE_EXCEPTION_ON_CONFLICT): Resource =
        copyAs(Resource(resource), copyStrategy)

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

    fun moveTo(directory: Resource, copyStrategy: CopyStrategy = CopyStrategy.RAISE_EXCEPTION_ON_CONFLICT): Resource {
        val resource = copyTo(directory, copyStrategy)
        remove()
        return resource
    }

    fun moveTo(directory: Path, copyStrategy: CopyStrategy = CopyStrategy.RAISE_EXCEPTION_ON_CONFLICT): Resource =
        moveTo(Resource(directory), copyStrategy)

    fun moveTo(directory: String, copyStrategy: CopyStrategy = CopyStrategy.RAISE_EXCEPTION_ON_CONFLICT): Resource =
        moveTo(Resource(directory), copyStrategy)

    fun openInputStream(): InputStream = path.inputStream()

    fun openOutputStream(): OutputStream = path.outputStream()

    fun createDirectory(): Resource {
        require(RESOURCE_DOES_NOT_EXIST, this)
        require(PARENT_DIRECTORY_EXISTS, this)
        return Resource(path.createDirectory())
    }

    fun createDirectories(): Resource {
        require(RESOURCE_DOES_NOT_EXIST, this)
        return Resource(path.createDirectories())
    }

    fun compressAsZip(destination: Resource, compressionLevel: Int = 5): Resource = CompressorFactory
        .create(CompressionType.ZIP, CompressionParameters.Creator().level(compressionLevel).create())
        .compress(this, destination)

    fun extractZip(directory: Resource? = null): List<Resource> = ExtractorFactory
        .create(CompressionType.ZIP)
        .extract(this, directory)

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

    private fun list(resource: Resource): List<Resource> =
        resource.path.listDirectoryEntries().map { Resource(it) }

    private fun removeContents(directory: Resource) =
        directory.list(Long.MAX_VALUE).reversed().forEach { Files.delete(it.path) }

    private fun copy(resource: Resource, copy: Resource, copyStrategy: CopyStrategy): Resource {
        if (copy.exists()) {
            when (copyStrategy) {
                CopyStrategy.RAISE_EXCEPTION_ON_CONFLICT ->
                    throw ResourceException(ResourceException.Type.RESOURCE_ALREADY_EXISTS, "\"$copy\" exists")
                CopyStrategy.OVERWRITE_ON_CONFLICT -> copy.remove()
            }
        }

        Files.copy(resource.path, copy.path)

        if (resource.isDirectory()) {
            resource.list(Long.MAX_VALUE).forEach {
                Files.copy(it.path, copy.path.resolve(resource.path.relativize(it.path)))
            }
        }

        return copy
    }
}
