package com.sawears.data.midijson

import com.sawears.data.midijson.InvalidMidiJsonException
import com.sawears.data.midijson.MidiJsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class MidiJsonParserTest {

    val parser = MidiJsonParser()

    @Test
    fun `parses valid input as expected`(){
        val input = """
            {
                "title": "Never gonna give you up",
                "composers": ["Rick Astley"],
                "year": 1987,
                "tempo": 120,
                "description": "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                
                "notes": [
                    {
                        "pitch": 60,
                        "duration": 3.0
                     },
                    {
                        "pitch": 80,
                        "duration": 1.5
                    }
                ]
             }
        """.trimIndent()
        val parsed = parser.parse(input)
        assertEquals("Never gonna give you up", parsed.title)
        assertEquals( "Rick Astley",parsed.composers[0])
        assertEquals(2, parsed.notes.size)
        assertEquals(1987,parsed.year)
        assertEquals(120,parsed.tempo)
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ",parsed.description)
        val note1 = parsed.notes[0]
        assertEquals(60,note1.pitch)
        assertEquals(3.0,note1.duration)
        val note2 = parsed.notes[1]
        assertEquals(80, note2.pitch)
        assertEquals(1.5, note2.duration)
    }

    @Test
    fun `fails on missing title`(){
        val parser = MidiJsonParser()

        val input = """
            {
                "composers": ["Rick Astley"],
                "year": 1987,
                "tempo": 120,
                "description": "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                
                "notes": [
                    {
                        "pitch": 60,
                        "duration": 3.0
                     },
                    {
                        "pitch": 80,
                        "duration": 1.5
                    }
                ]
             }
        """.trimIndent()

        assertFailsWith<InvalidMidiJsonException>{
            parser.parse(input)
        }
    }

    @Test
    fun `fails on missing composer`(){
        val parser = MidiJsonParser()

        val input = """
            {
                "title": "Never gonna give you up",
                "year": 1987,
                "tempo": 120,
                "description": "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                
                "notes": [
                    {
                        "pitch": 60,
                        "duration": 3.0
                     },
                    {
                        "pitch": 80,
                        "duration": 1.5
                    }
                ]
             }
        """.trimIndent()

        assertFailsWith<InvalidMidiJsonException>{
            parser.parse(input)
        }
    }

    @Test
    fun `fails on missing notes`(){
        val parser = MidiJsonParser()

        val input = """
            {
                "title": "Never gonna give you up",
                "composers": ["Rick Astley"],
                "year": 1987,
                "tempo": 120,
                "description": "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
             }
        """.trimIndent()

        assertFailsWith<InvalidMidiJsonException>{
            parser.parse(input)
        }
    }

    @Test
    fun `fails on missing note pitch`(){
        val parser = MidiJsonParser()

        val input = """
            {
                "title": "Never gonna give you up",
                "composers": ["Rick Astley"],
                "year": 1987,
                "tempo": 120,
                "description": "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                
                "notes": [
                    {
                        "pitch": 60,
                        "duration": 3.0
                     },
                    {
                        "duration": 1.5
                    }
                ]
             }
        """.trimIndent()

        assertFailsWith<InvalidMidiJsonException>{
            parser.parse(input)
        }
    }

    @Test
    fun `fails on missing note duration`(){
        val parser = MidiJsonParser()

        val input = """
            {
                "title": "Never gonna give you up",
                "composers": ["Rick Astley"],
                "year": 1987,
                "tempo": 120,
                "description": "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                
                "notes": [
                    {
                        "pitch": 60,
                        "duration": 3.0
                     },
                    {
                        "pitch": 80,
                    }
                ]
             }
        """.trimIndent()

        assertFailsWith<InvalidMidiJsonException>{
            parser.parse(input)
        }
    }
}