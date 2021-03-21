package org.cosybox.commons.io.compression

import org.cosybox.commons.io.Resource
import org.cosybox.commons.io.prerequisites.ResourceRequirement.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.cosybox.commons.io.prerequisites.ResourcePrerequisites.require as require

class ZipCompressor(val parameters: CompressionParameters) : Compressor {

    override fun compress(resources: List<Resource>, archive: Resource): Resource {
        resources.forEach { require(RESOURCE_EXISTS, it) }
        require(RESOURCE_DOES_NOT_EXIST, archive)
        require(RESOURCE_PARENT_EXIST, archive)
        require(RESOURCE_IS_DIRECTORY, archive.parent!!)
        val outputStream = ZipOutputStream(archive.openOutputStream())
        outputStream.setLevel(parameters.level)
        outputStream.use {
            resources.forEach { resource ->
                compress(resource, resource.name, it)
            }
        }
        return archive
    }

    private fun compress(resource: Resource, name: String, outputStream: ZipOutputStream) {
        if (resource.isDirectory()) {
            val entry = if (name.last() == Resource.PATH_SEPARATOR) name else "$name${Resource.PATH_SEPARATOR}"
            outputStream.putNextEntry(ZipEntry(entry))
            outputStream.closeEntry()
            resource.list().forEach { compress(it, "$name${Resource.PATH_SEPARATOR}${it.name}", outputStream) }
            return
        }

        outputStream.putNextEntry(ZipEntry(name))
        resource.openInputStream().use { it.copyTo(outputStream) }
        outputStream.closeEntry()
    }
}