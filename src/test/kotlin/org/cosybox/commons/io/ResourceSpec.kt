package org.cosybox.commons.io

import org.cosybox.commons.io.exceptions.ResourceException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

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

    @Test
    fun `Given a resource, that does not exist, throws ResourceException in case it is checked is it empty`() {
        assertThrows<ResourceException> {
            Resource("DIRECTORY_A").isEmpty()
        }
    }

    @Test
    fun `Given a resource, that is not a directory, throws ResourceException in case it is checked is it empty`() {
        assertThrows<ResourceException> {
            Resource(environment.createEmptyResource("FILE_A.txt")).isEmpty()
        }
    }

    @Test
    fun `Given a directory, returns that it is empty in case it does not contain any inner resources`() {
        assertTrue(Resource(environment.createDirectory("DIRECTORY_A")).isEmpty())
    }

    @Test
    fun `Given a directory, returns that is is not empty in case it does contain any inner resources`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A"))
        environment.createEmptyResource("DIRECTORY_A", "FILE_A.txt")

        assertFalse(directory.isEmpty())
    }

    @Test
    fun `Given a resource, that does not exist, throws ResourceException in case tried to calculate bytes count`() {
        assertThrows<ResourceException> {
            Resource("DIRECTORY_A").bytesCount()
        }
    }

    @Test
    fun `Given an empty directory, returns 0 in case tried to calculate bytes count`() {
        assertEquals(0, Resource(environment.createDirectory("DIRECTORY_A")).bytesCount())
    }

    @Test
    fun `Given an empty resource, returns 0 in case tried to calculate bytes count`() {
        assertEquals(0, Resource(environment.createEmptyResource("DIRECTORY_A")).bytesCount())
    }

    @Test
    fun `Given a non - empty resource, returns byte count in case tried to calculate bytes count`() {
        // UTF-8 enc (1 - 4 bytes per character)
        val resource = Resource(environment.createResource("FILE_A.txt", contents = "TEST_A"))

        assertEquals(6, resource.bytesCount())
    }

    @Test
    fun `Given a non - empty directory, returns byte count sum of directory contents in case tried to calculate bytes count`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A")) // 0
        environment.createResource("DIRECTORY_A", "FILE_A.txt", contents = "TEST_A") // 6 - 24
        environment.createDirectory("DIRECTORY_A", "DIRECTORY_B")
        environment.createResource("DIRECTORY_A", "DIRECTORY_B", "FILE_B.txt", contents = "TEST_B") // 6 - 24

        assertEquals(12, directory.bytesCount())
    }

    @Test
    fun `Given resource, that does not exist, throws ResourceException in case tried to remove resource`() {
        assertThrows<ResourceException> {
            Resource("FILE_A.txt").remove()
        }
    }

    @Test
    fun `Given resource, it is removed in case tried to remove resource`() {
        val resource = Resource(environment.createEmptyResource("FILE_A.txt"))
        resource.remove()

        assertFalse(exists(resource.path))
    }

    @Test
    fun `Given directory, it and it's contents are removed in case tried to remove resource`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A"))
        environment.createEmptyResource("DIRECTORY_A", "FILE_A.txt")
        environment.createDirectory("DIRECTORY_A", "DIRECTORY_B")
        environment.createEmptyResource("DIRECTORY_A", "DIRECTORY_B", "FILE_B.txt")
        directory.remove()

        assertFalse(exists(directory.path))
    }

    @Test
    fun `Given resource, that does not exist, throws ResourceException in case tried to remove it's contents`() {
        assertThrows<ResourceException> {
            Resource("FILE_A.txt").removeContents()
        }
    }

    @Test
    fun `Given resource, that is not a directory, throws ResourceException in case tried to remove it's contents`() {
        assertThrows<ResourceException> {
            Resource(environment.createEmptyResource("FILE_A.txt")).removeContents()
        }
    }

    @Test
    fun `Given directory, removes it's contents in case tried to remove it's contents`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A"))
        environment.createEmptyResource("DIRECTORY_A", "FILE_A.txt")
        environment.createDirectory("DIRECTORY_A", "DIRECTORY_B")
        environment.createEmptyResource("DIRECTORY_A", "DIRECTORY_B", "FILE_B.txt")
        directory.removeContents()

        assertTrue(exists(directory.path))
        assertTrue(list(directory.path).collect(Collectors.toList()).isEmpty())
    }

    @Test
    fun `Given resource, that does not exist, throws ResourceException in case it is tried to be copied into directory`() {
        assertThrows<ResourceException> {
            Resource("FILE_A.txt").copyTo(Resource(environment.createDirectory("DIRECTORY_A")))
        }
    }

    @Test
    fun `Given resource, throws ResourceException in case it is tried to be copied into directory, that does not exist`() {
        assertThrows<ResourceException> {
            Resource(environment.createEmptyResource("FILE_A.txt")).copyTo(Resource("DIRECTORY_A"))
        }
    }

    @Test
    fun `Given resource, throws ResourceException in case it is tried to be copied into resource, that is not a directory`() {
        assertThrows<ResourceException> {
            Resource(environment.createEmptyResource("FILE_A.txt")).copyTo(environment.createEmptyResource("FILE_B.txt"))
        }
    }

    @Test
    fun `Given resource, throws Resource exception in case it is tried to be copied into directory, but resource by that name exists in destination directory`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A"))
        environment.createEmptyResource("DIRECTORY_A", "FILE_A.txt")

        assertThrows<ResourceException> {
            Resource(environment.createEmptyResource("FILE_A.txt")).copyTo(directory)
        }
    }

    @Test
    fun `Given resource, it is copied into destination directory`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A"))
        val resource = Resource(environment.createResource("FILE_A.txt", contents = "TEST A"))
        val resourceCopied = resource.copyTo(directory)

        assertTrue(exists(resource.path))
        assertTrue(exists(resourceCopied.path))
        assertEquals(1, list(directory.path).count())
        assertEquals("TEST A", readString(resourceCopied.path))
    }

    @Test
    fun `Given directory, it and it's contents are copied into destination directory`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A"))
        val directoryToBeCopied = Resource(environment.createDirectory("DIRECTORY_B"))
        val resourceA = environment.createResource("DIRECTORY_B", "FILE_A.txt", contents = "TEST A")
        val resourceB = environment.createResource("DIRECTORY_B", "FILE_B.txt", contents = "TEST B")
        val resourceC = environment.createResource("DIRECTORY_B", "FILE_C.txt", contents = "TEST C")
        val directoryCopied = directoryToBeCopied.copyTo(directory)


        assertTrue(exists(directoryCopied.path))
        assertTrue(exists(resourceA))
        assertTrue(exists(resourceB))
        assertTrue(exists(resourceC))
        assertEquals(1, list(directory.path).count())
        assertEquals(3, list(environment.resourceAt("DIRECTORY_A", "DIRECTORY_B")).collect(Collectors.toList()).size)

        val resourceACopy = environment.resourceAt("DIRECTORY_A", "DIRECTORY_B", "FILE_A.txt")
        val resourceBCopy = environment.resourceAt("DIRECTORY_A", "DIRECTORY_B", "FILE_B.txt")
        val resourceCCopy = environment.resourceAt("DIRECTORY_A", "DIRECTORY_B", "FILE_C.txt")

        assertTrue(exists(resourceACopy))
        assertTrue(exists(resourceBCopy))
        assertTrue(exists(resourceCCopy))
        assertEquals("TEST A", readString(resourceACopy))
        assertEquals("TEST B", readString(resourceBCopy))
        assertEquals("TEST C", readString(resourceCCopy))
    }

    @Test
    fun `Given resource does not exist, throws ResourceException in case it is tried to be copied`() {
        assertThrows<ResourceException> {
            Resource("FILE_A.txt").copyAs(Resource("FILE_A copy.txt"))
        }
    }

    @Test
    fun `Given resource, throws ResourceException in case it is tried to be copied, but destination resource exists`() {
        val resource = Resource(environment.createEmptyResource("FILE_A.txt"))

        assertThrows<ResourceException> {
            resource.copyAs(resource)
        }
    }

    @Test
    fun `Given resource, throws ResourceException in case it is tried to be copied, but destination resource's parent directory does not exist`() {
        val resource = Resource(environment.createEmptyResource("FILE_A.txt"))
        val target = Paths
            .get(ResourcesEnvironment.RESOURCES_DIRECTORY)
            .resolve(environment.joinToPath("DIRECTORY_A", "FILE_A copy.txt"))

        assertThrows<ResourceException> {
            resource.copyAs(target)
        }
    }

    @Test
    fun `Given resource, throws ResourceException in case it is tried to be copied, but destination resource's parent is not a directory`() {
        val resource = Resource(environment.createEmptyResource("FILE_A.txt"))
        val target = environment.createEmptyResource("FILE_B.txt").resolve("FILE_A copy.txt")

        assertThrows<ResourceException> {
            resource.copyAs(target)
        }
    }

    @Test
    fun `Given resource, it is copied as provided destination resource`() {
        val resource = Resource(environment.createResource("FILE_A.txt", contents = "TEST A"))
        val copy = resource.copyAs(environment.createDirectory("DIRECTORY_A").resolve("FILE_A copy.txt"))

        assertTrue(exists(resource.path))
        assertEquals(1, list(copy.path.parent).count())
        assertTrue(exists(copy.path))
        assertEquals("TEST A", readString(copy.path))
    }

    @Test
    fun `Given directory, it and it's contents are copied as provided destination resource`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A"))
        environment.createResource("DIRECTORY_A", "FILE_A.txt", contents = "TEST A")
        environment.createResource("DIRECTORY_A", "FILE_B.txt", contents = "TEST B")
        environment.createResource("DIRECTORY_A", "FILE_C.txt", contents = "TEST C")
        val copy = directory
            .copyAs(Resource(Paths.get(ResourcesEnvironment.RESOURCES_DIRECTORY).resolve("DIRECTORY_A copy")))

        assertTrue(exists(copy.path))
        assertEquals(3, list(copy.path).count())
        assertEquals("TEST A", readString(environment.resourceAt("DIRECTORY_A copy", "FILE_A.txt")))
        assertEquals("TEST B", readString(environment.resourceAt("DIRECTORY_A copy", "FILE_B.txt")))
        assertEquals("TEST C", readString(environment.resourceAt("DIRECTORY_A copy", "FILE_C.txt")))
    }

    @Test
    fun `Given resource, that does not exist, throws ResourceException in case it's tried to be renamed`() {
        assertThrows<ResourceException> {
            Resource("FILE_A.txt").renameTo("FILE_B.txt")
        }
    }

    @Test
    fun `Given resource, throws IllegalArgumentException in case it's tried to be renamed into empty name`() {
        assertThrows<IllegalArgumentException> {
            Resource(environment.createEmptyResource("FILE_A.txt")).renameTo("")
        }
    }

    @Test
    fun `Given resource, throws ResourceException in case it's tried to be renamed, but resource, that has the name, exists`() {
        environment.createEmptyResource("FILE_B.txt")

        assertThrows<ResourceException> {
            Resource(environment.createEmptyResource("FILE_A.txt")).renameTo("FILE_B.txt")
        }
    }

    @Test
    fun `Given resource, it is renamed to provided name`() {
        val resource = Resource(environment.createResource("FILE_A.txt", contents = "TEST A"))
        val renamed = resource.renameTo("FILE_B.txt")

        assertFalse(exists(resource.path))
        assertTrue(exists(renamed.path))
        assertEquals("TEST A", readString(renamed.path))
    }

    @Test
    fun `Given resource, that does not exist, throws ResourceException in case it's tried to be moved`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A"))

        assertThrows<ResourceException> {
            Resource("FILE_A.txt").moveTo(directory)
        }
    }

    @Test
    fun `Given resource, throws ResourceException in case it's tried to be moved to a directory, that does not exist`() {
        assertThrows<ResourceException> {
            Resource(environment.createEmptyResource("FILE_A.txt")).moveTo(Resource("DIRECTORY_A"))
        }
    }

    @Test
    fun `Given resource, throws ResourceException in case it's tried to be moved to resource, that is not a directory`() {
        assertThrows<ResourceException> {
            Resource(environment.createEmptyResource("FILE_A.txt"))
                .moveTo(Resource(environment.createEmptyResource("FILE_B.txt")))
        }
    }

    @Test
    fun `Given resource, throws ResourceException in case resource by that name exist in provided directory`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A"))
        environment.createEmptyResource("DIRECTORY_A", "FILE_A.txt")

        assertThrows<ResourceException> {
            Resource(environment.createEmptyResource("FILE_A.txt")).moveTo(directory)
        }
    }

    @Test
    fun `Given resource, it is moved to provided directory`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A"))
        val resource = Resource(environment.createResource("FILE_A.txt", contents = "TEST A"))
        val moved = resource.moveTo(directory)

        assertFalse(exists(resource.path))
        assertTrue(exists(moved.path))
        assertEquals("TEST A", readString(environment.resourceAt("DIRECTORY_A", "FILE_A.txt")))
    }

    @Test
    fun `Given directory, it and it's contents are moved to provided directory`() {
        val directory = Resource(environment.createDirectory("DIRECTORY_A"))
        environment.createResource("DIRECTORY_A", "FILE_A.txt", contents = "TEST A")
        val moved = directory.moveTo(Resource(environment.createDirectory("DIRECTORY_B")))

        assertFalse(exists(directory.path))
        assertTrue(exists(moved.path))
        assertEquals(1, list(moved.path).count())
        assertEquals("TEST A", readString(environment.resourceAt("DIRECTORY_B", "DIRECTORY_A", "FILE_A.txt")))
    }

    @Test
    fun `Given input stream and path, throws ResourceException in case tried to create resource, that exists`() {
        val resource = Resource(environment.createResource("FILE_A.txt", contents = "TEST A"))

        assertThrows<ResourceException> {
            newInputStream(resource.path).use { inputStream ->
                Resource.createFromInputStream(inputStream, resource.path)
            }
        }
    }

    @Test
    fun `Given input stream and path, throws ResourceException in case tried to create resource at directory that does not exist`() {
        val resource = Resource(environment.createResource("FILE_A.txt", contents = "TEST A"))

        assertThrows<ResourceException> {
            newInputStream(resource.path).use { inputStream ->
                val path = Paths.get(ResourcesEnvironment.RESOURCES_DIRECTORY)
                    .resolve("DIRECTORY_A")
                    .resolve("FILE_A.txt")
                Resource.createFromInputStream(inputStream, path)
            }
        }
    }

    @Test
    fun `Given input stream and path, throws ResourceException in case tried to create resource that has no directory as a parent`() {
        val resource = Resource(environment.createResource("FILE_A.txt", contents = "TEST A"))
        environment.createEmptyResource("FILE_B.txt")

        assertThrows<ResourceException> {
            newInputStream(resource.path).use { inputStream ->
                val path = environment.resourceAt("FILE_B.txt").resolve("FILE_C.txt")
                Resource.createFromInputStream(inputStream, path)
            }
        }
    }

    @Test
    fun `Given input stream, creates resource`() {
        val resource = Resource(environment.createResource("FILE_A.txt", contents = "TEST A"))
        newInputStream(resource.path).use { inputStream ->
            val path = Paths.get(ResourcesEnvironment.RESOURCES_DIRECTORY).resolve("FILE_A copy.txt")
            Resource.createFromInputStream(inputStream, path)
        }

        val copy = environment.resourceAt("FILE_A copy.txt")

        assertTrue(exists(copy))
        assertEquals("TEST A", readString(copy))
    }
}
