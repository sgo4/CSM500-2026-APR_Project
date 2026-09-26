# Package com.sawears.domain
Domain level functionality including pitch estimation, transposition, settings management and exposing the song data model.

# Package com.sawears.domain.pitch
Provides real time pitch estimation features. The main entrypoint is the PitchEstimator interface.


# Package com.sawears.domain.settings
Provides domain layer business logic relating to settings management. The main public entry point is the DifficultySettingsProvider which assists with converting difficulty levels into more specific settigns that affect difficulty.

# Package com.sawears.domain.songModel
Exposes domain layer representations of songs for consumption by the view model. Owns the business logic for features that operate on Songs such as transposition.

# Package com.sawears.domain.useCases
Business logic representing specific user interactions.
