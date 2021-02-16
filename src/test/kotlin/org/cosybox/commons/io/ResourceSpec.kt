package org.cosybox.commons.io

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResourceSpec {

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
}
