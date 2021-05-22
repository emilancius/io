package org.cosybox.commons.io.compression

import org.cosybox.commons.io.Resource

interface Compressor {

    fun compress(resources: List<Resource>, destination: Resource): Resource

    fun compress(resource: Resource, destination: Resource): Resource =
        compress(listOf(resource), destination)
}