package org.cosybox.commons.io

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ResourceSpec {

    private val environment = ResourcesEnvironment()

    @BeforeEach
    fun setup() {
        environment.setup()
    }

    @AfterEach
    fun cleanup() {
        environment.cleanup()
    }

    @Test
    fun `Given empty path, in case Resource object is tried to be created, produces InstantiationException`() {
        assertThrows<InstantiationException> {
            Resource("")
        }
    }

    @Test
    fun `Resource object's 'name' parameter is set on object's creation`() {
        assertEquals("FILE_A.txt", Resource("FILE_A.txt").name)
        assertEquals("FILE_A.txt", Resource(environment.joinToPath("DIRECTORY_A", "FILE_A.txt")).name)
    }

    @Test
    fun `Resource object's 'extension' parameter is set on object's creation`() {
        assertEquals("txt", Resource("FILE_A.txt").extension)
        assertEquals("txt", Resource(".FILE_A.txt").extension)
        assertEquals(null, Resource(".FILE_A").extension)
        assertEquals(null, Resource(environment.joinToPath("DIRECTORY_A", "FILE_A")).extension)
    }

    @Test
    fun `Resource object's 'parent' parameter is set on object's creation`() {
        assertEquals(null, Resource("FILE_A.txt").parent)
        assertEquals(
            Resource(Resource.PATH_SEPARATOR.toString()),
            Resource("${Resource.PATH_SEPARATOR}DIRECTORY_A").parent
        )
        assertEquals(
            Resource(environment.joinToPath("DIRECTORY_A")),
            Resource(environment.joinToPath("DIRECTORY_A", "FILE_A.txt")).parent
        )
    }
}