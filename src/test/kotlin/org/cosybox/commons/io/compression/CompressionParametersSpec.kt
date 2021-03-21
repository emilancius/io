package org.cosybox.commons.io.compression

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CompressionParametersSpec {

    @Test
    fun `Given compression parameter 'level' is not in 0 to 9 range, produces IllegalArgumentException in case tried to create CompressionParameters`() {
        assertThrows<IllegalArgumentException> {
            CompressionParameters.Creator().level(10).create()
        }
    }

    @Test
    fun `Creates compression parameters`() {
        val parameters = CompressionParameters.Creator().level(7).create()
        assertEquals(parameters.level, 7)
    }

    @Test
    fun `In case compression parameter 'level' is not set, it is 5`() {
        assertEquals(5, CompressionParameters.Creator().create().level)
    }
}