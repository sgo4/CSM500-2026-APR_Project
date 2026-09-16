package com.sawears.data.midijson.parsers

import com.sawears.data.midijson.DTOs.MidiJsonFileDto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Parser for midi json files
 *
 * @property json a json serialization object
 */
internal class MidiJsonParser(
	private val json: Json = Json {
		ignoreUnknownKeys = false
	}
) {

    /**
     * Parse and validate midiJSON as text
     *
     * @param jsonText
     */
    fun parse(jsonText: String): MidiJsonFileDto {
        try {
            return json.decodeFromString<MidiJsonFileDto>(jsonText)
        } catch (e: SerializationException){
            throw InvalidMidiJsonException(
                "Unable to parse midiJSON: ${e.message}",
                cause = e
            )
        } catch (e: IllegalArgumentException){
            throw InvalidMidiJsonException(
                "midiJSON contains one or more invalid values: ${e.message}"
            )
        }

    }
}

/**
 * Error representing a malformed midiJSON file
 */
class InvalidMidiJsonException(
    message: String,
    cause: Throwable? = null
): Exception(message, cause)