# Package com.sawears.data
Provides data layer level functionality, including audio I/O, repositories, databases and parsers. See individual subpackage documentation for further detail.

# Package com.sawears.data.database
Provides data access objects (DAOs) for the application database


# Package com.sawears.data.io.audioIn
Provides public classes for audio input. The main public entrypoint is the AudioInputSource interface. Interfaces in this package generally require platform specific implemnetations.

# Package com.sawears.data.io.audioOut
Provides public classes for audio output (i.e. sound generation). The primary entrypoint is the AudioPlayer interface. Interfaces in this package generally require platform specific implementations.

# Package com.sawears.midijson
Provides repository for reading songs from mijson data. The main public entrypoint is the SongRepository

# Package com.sawears.settings
Provides repositories for accessing application and lesson-scoped settings. The main entrpoints are LessonSettingsRepository (lesson scoped) and SettingsRepository (application level)