package org.cosybox.commons.io.compression

import org.cosybox.commons.io.Resource

interface Extractor {

    fun extract(archive: Resource, directory: Resource? = null): List<Resource>
}