package org.cosybox.commons.io.compression

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompressorFactorySpec {

    @Test
    fun `Creates compressor by provided compressor type`() {
        val parameters = CompressionParameters.Creator().create()
        assertTrue(CompressorFactory.create(CompressionType.ZIP, parameters) is ZipCompressor)
    }
}