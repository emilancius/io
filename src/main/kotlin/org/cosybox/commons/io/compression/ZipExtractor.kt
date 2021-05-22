package org.cosybox.commons.io.compression

import org.cosybox.commons.io.Resource
import org.cosybox.commons.io.exceptions.ResourceException
import org.cosybox.commons.io.prerequisites.ResourceRequirements.Companion.require
import org.cosybox.commons.io.prerequisites.ResourceRequirements.Requirement.*
import java.util.zip.ZipInputStream
import kotlin.jvm.Throws

class ZipExtractor : Extractor {

    @Throws(ResourceException::class)
    override fun extract(source: Resource, directory: Resource?): List<Resource> {
        require(RESOURCE_EXISTS, source)
        val dir = directory
            ?.let {
                require(PARENT_DIRECTORY_EXISTS, directory)
                it
            }
            ?: source.parent!!

        if (!dir.exists()) {
            dir.createDirectory()
        }

        val resources = ArrayList<Resource>()

        ZipInputStream(source.openInputStream()).use { inputStream ->
            var entry = inputStream.nextEntry

            while (entry != null) {
                val path = dir.path.resolve(entry.name)
                val resource =
                    if (entry.isDirectory) {
                        Resource(path).createDirectory()
                    } else {
                        Resource.createFromInputStream(inputStream, path)
                    }
                resources.add(resource)
                inputStream.closeEntry()
                entry = inputStream.nextEntry
            }
        }

        return resources
    }
}