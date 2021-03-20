package org.cosybox.commons.io.exception

class ResourceException(val type: Type, message: String? = null) : RuntimeException(message) {

    enum class Type {
        RESOURCE_ALREADY_EXISTS,
        RESOURCE_DOES_NOT_EXIST,
        RESOURCE_IS_NOT_DIRECTORY,
        RESOURCE_PARENT_DOES_NOT_EXIST
    }
}