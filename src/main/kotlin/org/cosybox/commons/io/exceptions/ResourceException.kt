package org.cosybox.commons.io.exceptions

import java.io.IOException

class ResourceException(val type: Type, message: String?) : IOException(message) {
    enum class Type {
        RESOURCE_ALREADY_EXISTS,
        RESOURCE_DOES_NOT_EXIST,
        RESOURCE_IS_NOT_A_DIRECTORY,
        PARENT_DIRECTORY_DOES_NOT_EXIST
    }
}