package org.cosybox.commons.io.compression

import org.cosybox.commons.io.Resource
import org.cosybox.commons.io.ResourcesEnvironment
import org.cosybox.commons.io.exceptions.ResourceException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ZipCompressorSpec {

    private val environment = ResourcesEnvironment()
    private val compressor = ZipCompressor(CompressionParameters.Creator().create())

    @BeforeEach
    fun setup() = environment.setup()

    @AfterEach
    fun cleanup() = environment.cleanup()

    @Test
    fun `Given resources to compress, and at least one does not exist, produces ResourceException in case tried to compress it to ZIP archive`() {
        val resources = listOf(
            environment.createResource("FILE_A.txt"),
            Resource(environment.joinToPath(ResourcesEnvironment.ROOT_DIRECTORY, "FILE_B.txt"))
        )

        assertThrows<ResourceException> {
            compressor.compress(
                resources,
                Resource(environment.joinToPath(ResourcesEnvironment.ROOT_DIRECTORY, "ARCHIVE.zip"))
            )
        }
    }

    @Test
    fun `Given resource to compress, produces ResourceException in case tried to compress resource, but archive exists`() {
        val resource = environment.createResource("FILE_A.txt")
        val archive = environment.createResource("ARCHIVE.zip")

        assertThrows<ResourceException> {
            compressor.compress(resource, archive)
        }
    }

    @Test
    fun `Given resource to compress, produces ResourceException in case tried to compress resource, but archive's parent directory structure does not exist`() {
        val resource = environment.createResource("FILE_A.txt")

        assertThrows<ResourceException> {
            compressor.compress(
                resource,
                Resource(environment.joinToPath(ResourcesEnvironment.ROOT_DIRECTORY, "DIRECTORY_A", "ARCHIVE.zip"))
            )
        }
    }

    @Test
    fun `Given resource to compress, compresses resources into an archive`() {
        val resources = listOf(
            environment.createResource("FILE_A.txt"),
            environment.createDirectory("DIRECTORY_A"),
        )
        environment.createResource("DIRECTORY_A", "FILE_B.txt")

        val archive = compressor.compress(
            resources,
            Resource(environment.joinToPath(ResourcesEnvironment.ROOT_DIRECTORY, "ARCHIVE.zip"))
        )

        assertEquals(Resource(environment.joinToPath(ResourcesEnvironment.ROOT_DIRECTORY, "ARCHIVE.zip")), archive)
        assertTrue(archive.bytesCount() > 0)
    }
}