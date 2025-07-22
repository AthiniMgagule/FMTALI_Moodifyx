# Moodifyx 🎵

A mood-based music player application built in Java Swing that recommends and plays music based on your current emotional state.

## Description

Moodifyx is a desktop music player that helps you discover the perfect soundtrack for your mood. Simply select how you're feeling from a variety of emotional states, and the application will randomly play songs that match your vibe. Whether you're feeling happy, angry, chill, or romantic, Moodifyx has curated music to complement your emotional journey.

The application features an intuitive graphical interface built with Java Swing and uses Java's built-in audio capabilities to play WAV files. It's designed to be simple, lightweight, and focused on enhancing your mood through music.

## Features

- **Mood-Based Music Selection**: Choose from 9 different moods including Happy, Angry, Chill, Moody, Mysterious, Peaceful, Positive, Practice, and Romantic
- **Random Song Playback**: Automatically selects random songs from your chosen mood category
- **Interactive Playback Controls**: Options to play another song, change mood, or exit during playback
- **Clean GUI Interface**: Simple and intuitive Swing-based user interface
- **Audio Playback Support**: Built-in WAV file playback using Java Sound API

## Prerequisites

- Java 8 or higher
- WAV audio files in the `music/` directory following the naming convention

## Installation & Setup

1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/moodifyx.git
   cd moodifyx
   ```

2. Create a `music/` directory in your project root

3. Add your WAV audio files following this naming pattern:
   ```
   music/
   ├── happygirl.wav
   ├── happygirlie.wav
   ├── angrygirl.wav
   ├── angrygirlie.wav
   ├── chillgirl.wav
   ├── chillgirlie.wav
   └── ... (continue for all moods)
   ```

4. Compile the Java files:
   ```bash
   javac -d . src/com/moodifyx/*.java
   ```

5. Run the application:
   ```bash
   java com.moodifyx.Main
   ```

## Usage

1. Launch the application
2. Select your current mood from the dropdown menu
3. Click "Recommend Music" to start playback
4. Use the dialog options to:
   - Play another song from the same mood
   - Change to a different mood
   - Stop music and exit the application

## Project Structure

```
src/
├── com/moodifyx/
│   ├── Main.java              # Application entry point
│   ├── MoodSelectorUI.java    # Main GUI interface
│   ├── MoodRepository.java    # Manages mood-to-song mappings
│   ├── Song.java              # Song data model
│   └── SongPlayer.java        # Audio playback functionality
└── music/                     # Audio files directory
```

## Architecture

- **Model-Repository Pattern**: `MoodRepository` manages the data layer for mood-song relationships
- **MVC Architecture**: Clear separation between UI (`MoodSelectorUI`), data (`Song`, `MoodRepository`), and audio logic (`SongPlayer`)
- **Event-Driven GUI**: Swing components with action listeners for user interactions

## Available Moods

- **Happy**: Upbeat and joyful tracks
- **Angry**: High-energy, intense music
- **Chill**: Relaxed and laid-back vibes
- **Moody**: Emotional and introspective songs
- **Mysterious**: Enigmatic and atmospheric tracks
- **Peaceful**: Calm and serene melodies
- **Positive**: Uplifting and motivational music
- **Practice**: Focus-friendly background music
- **Romantic**: Love songs and intimate ballads

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-mood`)
3. Commit your changes (`git commit -am 'Add new mood category'`)
4. Push to the branch (`git push origin feature/new-mood`)
5. Create a Pull Request

## Future Enhancements

- Support for additional audio formats (MP3, FLAC, etc.)
- Playlist creation and management
- Volume control
- Mood detection based on external factors
- User-customizable mood categories
- Music streaming integration

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with Java Swing for cross-platform compatibility
- Uses Java Sound API for audio playback
- Designed with simplicity and user experience in mind
