package org.cosybox.commons.io.prerequisites

import org.cosybox.commons.io.Resource
import org.cosybox.commons.io.exceptions.ResourceException
import org.cosybox.commons.io.exceptions.ResourceException.Type.*
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class ResourceRequirements {
    enum class Requirement {
        RESOURCE_EXISTS,
        RESOURCE_DOES_NOT_EXIST,
        PARENT_DIRECTORY_EXISTS,
        RESOURCE_IS_DIRECTORY
    }

    companion object {
        fun require(requirement: Requirement, resource: Resource, message: String? = null) {
            val path = resource.path

            when (requirement) {
                Requirement.RESOURCE_EXISTS -> {
                    if (!path.exists()) {
                        throw ResourceException(
                            type = RESOURCE_DOES_NOT_EXIST,
                            message = message ?: "\"$path\" could not be found"
                        )
                    }
                }
                Requirement.RESOURCE_DOES_NOT_EXIST -> {
                    if (path.exists()) {
                        throw ResourceException(
                            type = RESOURCE_ALREADY_EXISTS,
                            message = message ?: "\"$path\" exists"
                        )
                    }
                }
                Requirement.PARENT_DIRECTORY_EXISTS -> {
                    if (resource.parent == null || !resource.parent.path.exists()) {
                        throw ResourceException(
                            type = PARENT_DIRECTORY_DOES_NOT_EXIST,
                            message = message ?: "\"Parent directory \"${path.parent}\" could not be found\""
                        )
                    }
                }
                Requirement.RESOURCE_IS_DIRECTORY -> {
                    if (!path.isDirectory()) {
                        throw ResourceException(
                            type = RESOURCE_IS_NOT_A_DIRECTORY,
                            message = message ?: "\"$path\" is not a directory"
                        )
                    }
                }
            }
        }
    }
}