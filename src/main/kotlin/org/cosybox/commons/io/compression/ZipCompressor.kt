package org.cosybox.commons.io.compression

import org.cosybox.commons.io.Resource
import org.cosybox.commons.io.exceptions.ResourceException
import org.cosybox.commons.io.prerequisites.ResourceRequirements.Companion.require
import org.cosybox.commons.io.prerequisites.ResourceRequirements.Requirement.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.jvm.Throws

class ZipCompressor(private val parameters: CompressionParameters) : Compressor {

    @Throws(ResourceException::class)
    override fun compress(resources: List<Resource>, destination: Resource): Resource {
        resources.forEach {
            require(RESOURCE_EXISTS, it)
        }
        require(RESOURCE_DOES_NOT_EXIST, destination)
        require(PARENT_DIRECTORY_EXISTS, destination)
        val outputStream = ZipOutputStream(destination.openOutputStream())
        outputStream.setLevel(parameters.level)
        outputStream.use {
            resources.forEach { resource ->
                compress(resource, resource.name, it)
            }
        }
        return destination
    }

    private fun compress(resource: Resource, name: String, outputStream: ZipOutputStream) {
        if (resource.isDirectory()) {
            val entry = if (name.last() == Resource.PATH_SEPARATOR) name else "$name${Resource.PATH_SEPARATOR}"
            outputStream.putNextEntry(ZipEntry(entry))
            outputStream.closeEntry()
            resource.list().forEach {
                compress(it, "$name${Resource.PATH_SEPARATOR}${it.name}", outputStream)
            }
            return
        }

        outputStream.putNextEntry(ZipEntry(name))
        resource.openInputStream().use { it.copyTo(outputStream) }
        outputStream.closeEntry()
    }
}