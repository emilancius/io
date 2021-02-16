package org.cosybox.commons.io

import org.cosybox.commons.io.prerequisites.ResourcePrerequisites.resourceExists
import org.cosybox.commons.io.prerequisites.ResourcePrerequisites.resourceIsDirectory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Resource(val path: Path) {

    companion object {
        private val PATH_SEPARATOR: Char = File.separatorChar
        private const val EXTENSION_SEPARATOR: Char = '.'
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
}