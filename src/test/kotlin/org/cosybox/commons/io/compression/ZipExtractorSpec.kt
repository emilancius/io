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
import java.nio.file.Files.exists

class ZipExtractorSpec {

    private val environment = ResourcesEnvironment()
    private val compressor = ZipCompressor(CompressionParameters.Creator().create())
    private val extractor = ZipExtractor()

    @BeforeEach
    fun setup() = environment.setup()

    @AfterEach
    fun cleanup() = environment.cleanup()

    @Test
    fun `Given archive, that does not exists, produces ResourceException in case tried to extract it`() {
        assertThrows<ResourceException> {
            extractor.extract(Resource(environment.joinToPath(ResourcesEnvironment.ROOT_DIRECTORY, "ARCHIVE.zip")))
        }
    }

    @Test
    fun `Given archive, it is extracted into provided directory`() {
        val resources = listOf(
            environment.createResource("FILE_A.txt"),
            environment.createDirectory("DIRECTORY_A")
        )
        environment.createResource("DIRECTORY_A", "FILE_B.txt")
        val archive = compressor.compress(
            resources,
            Resource(environment.joinToPath(ResourcesEnvironment.ROOT_DIRECTORY, "ARCHIVE.zip"))
        )
        val extracted = extractor.extract(
            archive,
            Resource(environment.joinToPath(ResourcesEnvironment.ROOT_DIRECTORY, "DIRECTORY_B"))
        )

        assertEquals(3, extracted.size)
        extracted.forEach {
            assertTrue(exists(it.path))
        }
    }

    @Test
    fun `Given archive, it is extracted into archive's parent directory`() {
        val resources = listOf(
            environment.createResource("FILE_A.txt"),
            environment.createDirectory("DIRECTORY_A")
        )
        environment.createResource("DIRECTORY_A", "FILE_B.txt")
        val archive = compressor.compress(
            resources,
            Resource(environment.joinToPath(ResourcesEnvironment.ROOT_DIRECTORY, "ARCHIVE.zip"))
        )
        resources.forEach { environment.removeResource(it.path) }
        val extracted = extractor.extract(archive)

        assertEquals(3, extracted.size)
        extracted.forEach {
            assertTrue(exists(it.path))
        }
    }
}