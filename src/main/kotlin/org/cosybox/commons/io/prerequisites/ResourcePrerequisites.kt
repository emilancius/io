package org.cosybox.commons.io.prerequisites

import org.cosybox.commons.io.Resource
import org.cosybox.commons.io.exceptions.ResourceException
import java.nio.file.Files.*

object ResourcePrerequisites {

    fun resourceExists(resource: Resource) {
        if (!exists(resource.path)) {
            throw ResourceException("Resource \"$resource\" could not be found")
        }
    }

    fun resourceIsDirectory(resource: Resource) {
        if (!isDirectory(resource.path)) {
            throw ResourceException("Resource \"$resource\" is not a directory")
        }
    }
}