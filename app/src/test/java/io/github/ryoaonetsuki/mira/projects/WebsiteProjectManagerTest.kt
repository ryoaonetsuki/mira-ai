package io.github.ryoaonetsuki.mira.projects

import org.junit.Assert.*
import org.junit.Test

class WebsiteProjectManagerTest {

    @Test
    fun testExtractFilesFromContent_ExtractsSubdirectoryFiles() {
        val raw = """
            ### Project Title
            A multi-page website.
            
            [Open Website](mira://project/mysite)
            
            <<<FILE:pages/about.html>>>
            <!DOCTYPE html>
            <html>
            <body>About page</body>
            </html>
            <<<ENDFILE>>>
            <<<FILE:index.html>>>
            <!DOCTYPE html>
            <html>
            <body>Home page</body>
            </html>
            <<<ENDFILE>>>
        """.trimIndent()

        val files = WebsiteProjectManager.extractFilesFromContent(raw)
        assertEquals(2, files.size)
        assertTrue(files.containsKey("pages/about.html"))
        assertTrue(files.containsKey("index.html"))
        assertEquals("<!DOCTYPE html>\n<html>\n<body>About page</body>\n</html>", files["pages/about.html"])
    }

    @Test
    fun testExtractFilesFromContent_ExtractsPathTraversalFilename() {
        // Extraction still captures the filename; path-traversal rejection happens at save time.
        val raw = """
            <<<FILE:../evil.html>>>
            <html>evil</html>
            <<<ENDFILE>>>
        """.trimIndent()

        val files = WebsiteProjectManager.extractFilesFromContent(raw)
        assertTrue(files.containsKey("../evil.html"))
    }
}
