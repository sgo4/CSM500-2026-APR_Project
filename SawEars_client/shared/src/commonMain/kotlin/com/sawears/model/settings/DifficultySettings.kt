package com.sawears.model.settings


/**
 * Data  class to hold the concrete values representing a given difficulty level.
 * In the current implementation difficulty only concerns cents difference but this
 * could be extended.
 */
data class DifficultySettings(
    val centsThreshold: Float
)
/**
 * Translates difficulty into concrete application settings
 */
interface DifficultySettingsProvider {

    /**
     * Translate difficulty enum into concrete settings
     */
    fun get(difficulty: Difficulty): DifficultySettings
}

/**
 * Standard difficulty implementation
 */
class StandardDifficultySettingsProvider: DifficultySettingsProvider {
    override fun get(difficulty: Difficulty): DifficultySettings =
        when (difficulty){
            Difficulty.EASIEST -> DifficultySettings(60f)
            Difficulty.EASY -> DifficultySettings(55f)
            Difficulty.STANDARD -> DifficultySettings(50f)
            Difficulty.HARD -> DifficultySettings(45f)
            Difficulty.HARDEST -> DifficultySettings(40f)
        }
}


/**
 * Enum to translate data layer int difficulty representation
 * to enum that can be handled at the domain layer.
 *
 * Implementation note: An enum is prefereable here as it makes it easier to guarantee
 * that all difficulties are handled
 */
enum class Difficulty(val level: Int){

    EASIEST(1),
    EASY(2),
    STANDARD(3),
    HARD(4),
    HARDEST(5);

    companion object {
        fun from(level: Int): Difficulty =
            entries.firstOrNull { it.level == level }
                ?: error("Invalid difficulty: $level")
    }
}