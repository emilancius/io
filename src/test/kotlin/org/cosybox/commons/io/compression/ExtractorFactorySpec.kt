package org.cosybox.commons.io.compression

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExtractorFactorySpec {

    @Test
    fun `Creates extractor by provided compression type`() {
        assertTrue(ExtractorFactory.create(CompressionType.ZIP) is ZipExtractor)
    }
}