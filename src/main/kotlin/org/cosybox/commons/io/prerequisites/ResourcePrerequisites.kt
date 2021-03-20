package org.cosybox.commons.io.prerequisites

import org.cosybox.commons.io.Resource
import org.cosybox.commons.io.exception.ResourceException
import java.nio.file.Files.*

object ResourcePrerequisites {

    fun require(requirement: ResourceRequirement, resource: Resource, message: String? = null) {
        var path = resource.path

        when (requirement) {
            ResourceRequirement.RESOURCE_EXISTS -> {
                if (!exists(path)) {
                    throw ResourceException(
                        ResourceException.Type.RESOURCE_DOES_NOT_EXIST,
                        message ?: "\"$path\" could not be found"
                    )
                }
            }
            ResourceRequirement.RESOURCE_DOES_NOT_EXIST -> {
                if (exists(path)) {
                    throw ResourceException(
                        ResourceException.Type.RESOURCE_ALREADY_EXISTS,
                        message ?: "\"$path\" exists"
                    )
                }
            }
            ResourceRequirement.RESOURCE_IS_DIRECTORY -> {
                if (!isDirectory(path)) {
                    throw ResourceException(
                        ResourceException.Type.RESOURCE_IS_NOT_DIRECTORY,
                        message ?: "\"$path\" is not a directory"
                    )
                }
            }
            ResourceRequirement.RESOURCE_PARENT_EXIST -> {
                if (resource.parent == null || !exists(path.parent)) {
                    throw ResourceException(
                        ResourceException.Type.RESOURCE_PARENT_DOES_NOT_EXIST,
                        message ?: "Parent directory \"${path.parent}\" could not be found"
                    )
                }
            }
        }
    }
}