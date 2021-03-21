package org.cosybox.commons.io.compression

import org.cosybox.commons.io.Resource
import org.cosybox.commons.io.prerequisites.ResourcePrerequisites.require as require
import org.cosybox.commons.io.prerequisites.ResourceRequirement.*
import java.util.zip.ZipInputStream


class ZipExtractor : Extractor {

    override fun extract(archive: Resource, directory: Resource?): List<Resource> {
        require(RESOURCE_EXISTS, archive)
        val dir = directory
            ?.let {
                require(RESOURCE_PARENT_EXIST, it)
                it
            } ?: archive.parent!!

        if (!dir.exists()) {
            dir.createDirectory()
        }

        val resources = ArrayList<Resource>()

        ZipInputStream(archive.openInputStream()).use { inputStream ->
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