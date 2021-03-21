package org.cosybox.commons.io.compression

import org.cosybox.commons.io.Resource

interface Compressor {

    fun compress(resources: List<Resource>, archive: Resource): Resource

    fun compress(resource: Resource, archive: Resource): Resource = compress(listOf(resource), archive)
}