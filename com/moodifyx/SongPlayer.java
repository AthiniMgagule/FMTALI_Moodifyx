package com.moodifyx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class SongPlayer {
    private static Clip clip;
    private static FloatControl volumeControl;
    private static long clipPosition = 0;
    private static boolean isPaused = false;
    private static List<PlaybackListener> listeners = new ArrayList<>();
    private static Thread progressThread;
    private static boolean isPlaying = false;
    
    public interface PlaybackListener {
        void onPositionChanged(long position, long duration);
        void onPlaybackStopped();
        void onPlaybackStarted();
    }
    
    public static void addPlaybackListener(PlaybackListener listener) {
        listeners.add(listener);
    }
    
    public static void removePlaybackListener(PlaybackListener listener) {
        listeners.remove(listener);
    }
    
    private static void notifyPositionChanged(long position, long duration) {
        for (PlaybackListener listener : listeners) {
            listener.onPositionChanged(position, duration);
        }
    }
    
    private static void notifyPlaybackStopped() {
        for (PlaybackListener listener : listeners) {
            listener.onPlaybackStopped();
        }
    }
    
    private static void notifyPlaybackStarted() {
        for (PlaybackListener listener : listeners) {
            listener.onPlaybackStarted();
        }
    }

    public static void play(String filePath) {
        try {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filePath));
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            // Get volume control
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            }
            
            clip.start();
            isPlaying = true;
            isPaused = false;
            clipPosition = 0;
            
            notifyPlaybackStarted();
            startProgressTracking();
            
        } catch (Exception e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
    }
    
    public static void pause() {
        if (clip != null && clip.isRunning()) {
            clipPosition = clip.getMicrosecondPosition();
            clip.stop();
            isPaused = true;
            isPlaying = false;
            stopProgressTracking();
        }
    }
    
    public static void resume() {
        if (clip != null && isPaused) {
            clip.setMicrosecondPosition(clipPosition);
            clip.start();
            isPaused = false;
            isPlaying = true;
            startProgressTracking();
        }
    }
    
    public static void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clipPosition = 0;
            isPaused = false;
            isPlaying = false;
            stopProgressTracking();
            notifyPlaybackStopped();
        }
    }
    
    public static void setVolume(float volume) {
        if (volumeControl != null) {
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float gain = min + (max - min) * volume;
            volumeControl.setValue(gain);
        }
    }
    
    public static void setPosition(long microseconds) {
        if (clip != null && clip.isOpen()) {
            boolean wasPlaying = isPlaying;
            clip.stop();
            clip.setMicrosecondPosition(microseconds);
            clipPosition = microseconds;
            if (wasPlaying) {
                clip.start();
            }
        }
    }
    
    public static long getCurrentPosition() {
        if (clip != null && clip.isOpen()) {
            return isPaused ? clipPosition : clip.getMicrosecondPosition();
        }
        return 0;
    }
    
    public static long getDuration() {
        if (clip != null && clip.isOpen()) {
            return clip.getMicrosecondLength();
        }
        return 0;
    }
    
    public static boolean isPlaying() {
        return isPlaying && clip != null && clip.isRunning();
    }
    
    public static boolean isPaused() {
        return isPaused;
    }
    
    private static void startProgressTracking() {
        stopProgressTracking();
        progressThread = new Thread(() -> {
            while (isPlaying && clip != null && clip.isOpen()) {
                try {
                    long position = getCurrentPosition();
                    long duration = getDuration();
                    notifyPositionChanged(position, duration);
                    
                    // Check if song ended
                    if (position >= duration - 1000) { // 1ms tolerance
                        stop();
                        break;
                    }
                    
                    Thread.sleep(100); // Update every 100ms
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.out.println("Error in progress tracking: " + e.getMessage());
                    break;
                }
            }
        });
        progressThread.setDaemon(true);
        progressThread.start();
    }
    
    private static void stopProgressTracking() {
        if (progressThread != null && progressThread.isAlive()) {
            progressThread.interrupt();
        }
    }
}
