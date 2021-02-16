package org.cosybox.commons.io

import org.cosybox.commons.io.exceptions.ResourceException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Path

class ResourceSpec {

    private val environment = ResourcesEnvironment()

    @BeforeEach
    fun setup() = environment.setup()

    @AfterEach
    fun cleanup() = environment.cleanup()

    @Test
    fun `Resource's name parameter is set on object's creation`() {
        assertEquals("DIRECTORY_B", Resource("/DIRECTORY_A/DIRECTORY_B/").name)
        assertEquals("FILE_A.txt", Resource("/DIRECTORY_A/DIRECTORY_B/FILE_A.txt").name)
    }

    @Test
    fun `Resource's extension parameter is set on object's creation`() {
        assertEquals("txt", Resource("/DIRECTORY_A/DIRECTORY_B/FILE_A.txt").extension)
        assertEquals("txt", Resource("/DIRECTORY_A/DIRECTORY_B/.FILE_A.txt").extension)
        assertEquals(null, Resource("/DIRECTORY_A/DIRECTORY_B/FILE_A").extension)
        assertEquals(null, Resource("/DIRECTORY_A/DIRECTORY_B/.FILE_A").extension)
    }

    @Test
    fun `Resource's parent is set on object's creation`() {
        assertEquals(null, Resource("/").parent)
        assertEquals(Resource("/"), Resource("/DIRECTORY_A").parent)
        assertEquals(Resource("/"), Resource("/DIRECTORY_A/").parent)
        assertEquals(Resource("/DIRECTORY_A"), Resource("/DIRECTORY_A/DIRECTORY_B").parent)
        assertEquals(Resource("/DIRECTORY_A"), Resource("/DIRECTORY_A/DIRECTORY_B/").parent)
    }

    @Test
    fun `Given that resource exists, returns that resource exist`() {
        val resource = Resource(environment.createEmptyResource("FILE_A.txt"))

        assertTrue(resource.exists())
    }

    @Test
    fun `Given that resource does not exist, returns that resource could not be found`() {
        assertFalse(Resource("FILE_A.txt").exists())
    }

    @Test
    fun `Given that resource is directory, returns that resource is directory`() {
        val resource = Resource(environment.createDirectory("DIRECTORY_A"))

        assertTrue(resource.isDirectory())
    }

    @Test
    fun `Given that resource is not a directory, returns that resource is not a directory`() {
        val resource = Resource(environment.createEmptyResource("FILE_A.txt"))

        assertFalse(resource.isDirectory())
    }

    @Test
    fun `Given, that resource does not exist, throws ResourceException in case resource is checked is it a directory`() {
        assertThrows<ResourceException> {
            Resource("FILE_A.txt").isDirectory()
        }
    }

    @Test
    fun `Given, that directory does not exist, throws ResourceException in case tried to list it's contents`() {
        assertThrows<ResourceException> {
            Resource("DIRECTORY_A").list()
        }
    }

    @Test
    fun `Given, that resource is not a directory, throw ResourceException in case tried to list it's contents`() {
        assertThrows<ResourceException> {
            Resource(environment.createEmptyResource("FILE_A.txt")).list()
        }
    }

    @Test
    fun `In case tried to list directory contents at depth less than 1, returns no content`() {
        val directory = environment.createDirectory("DIRECTORY_A")
        environment.createEmptyResource("DIRECTORY_A", "FILE_A.txt")

        assertEquals(0, Resource(directory).list(depth = 0).size)
    }

    @Test
    fun `Lists directory contents at provided depth`() {
        val directory = environment.createDirectory("DIRECTORY_A")
        val contents = ArrayList<Path>()
        contents.add(environment.createEmptyResource("DIRECTORY_A", "FILE_A.txt"))
        contents.add(environment.createDirectory("DIRECTORY_A", "DIRECTORY_B"))
        contents.add(environment.createEmptyResource("DIRECTORY_A", "DIRECTORY_B", "FILE_B.txt"))
        contents.add(environment.createDirectory("DIRECTORY_A", "DIRECTORY_B", "DIRECTORY_C"))
        contents.add(environment.createEmptyResource("DIRECTORY_A", "DIRECTORY_B", "DIRECTORY_C", "FILE_C.txt"))

        val resources = Resource(directory).list(depth = 3)

        assertEquals(5, resources.size)
        contents.forEach { assertTrue(resources.contains(Resource(it))) }
    }
}
