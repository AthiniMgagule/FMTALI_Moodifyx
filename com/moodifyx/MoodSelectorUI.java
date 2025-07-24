package com.moodifyx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class MoodSelectorUI extends JFrame implements SongPlayer.PlaybackListener {
    private final MoodRepository repository = new MoodRepository();
    
    // UI Components
    private JComboBox<String> moodDropdown;
    private JButton playPauseBtn;
    private JButton stopBtn;
    private JButton prevBtn;
    private JButton nextBtn;
    private JButton shuffleBtn;
    private JButton repeatBtn;
    private JSlider volumeSlider;
    private JSlider progressSlider;
    private JLabel nowPlayingLabel;
    private JLabel timeLabel;
    private JPanel nowPlayingPanel;
    private JButton themeToggleBtn;
    
    // State variables
    private List<Song> currentMoodSongs;
    private Song currentSong;
    private int currentSongIndex = 0;
    private boolean shuffleMode = false;
    private boolean repeatMode = false;
    private boolean isDarkTheme = false;
    private boolean updatingProgress = false;
    
    // Theme colors
    private Color lightBg = Color.WHITE;
    private Color lightFg = Color.BLACK;
    private Color lightPanel = new Color(245, 245, 245);
    private Color darkBg = new Color(45, 45, 45);
    private Color darkFg = Color.WHITE;
    private Color darkPanel = new Color(60, 60, 60);

    public MoodSelectorUI() {
        setTitle("Moodifyx - Enhanced Music Player");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        SongPlayer.addPlaybackListener(this);
        setupUI();
        setupKeyboardShortcuts();
        applyTheme();
        setVisible(true);
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Top panel - Mood selection and theme toggle
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));
        
        Set<String> moods = repository.getAllMoods();
        moodDropdown = new JComboBox<>(moods.toArray(new String[0]));
        
        JButton selectMoodBtn = new JButton("Load Mood");
        selectMoodBtn.addActionListener(e -> loadMoodSongs());
        
        //======= toggle theme
        themeToggleBtn = new JButton("🌙");
        themeToggleBtn.setToolTipText("Toggle Dark/Light Theme");
        themeToggleBtn.addActionListener(e -> toggleTheme());
        
        topPanel.add(new JLabel("Select Mood:"));
        topPanel.add(moodDropdown);
        topPanel.add(selectMoodBtn);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(themeToggleBtn);
        
        //======== Center panel - Now playing info
        setupNowPlayingPanel();
        
        //======== Bottom panel - Controls
        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        //======= Playback controls
        JPanel playbackPanel = new JPanel(new FlowLayout());
        prevBtn = new JButton("⏮");
        playPauseBtn = new JButton("▶");
        stopBtn = new JButton("⏹");
        nextBtn = new JButton("⏭");
        shuffleBtn = new JButton("🔀");
        repeatBtn = new JButton("🔁");
        
        prevBtn.addActionListener(e -> previousSong());
        playPauseBtn.addActionListener(e -> togglePlayPause());
        stopBtn.addActionListener(e -> stopPlayback());
        nextBtn.addActionListener(e -> nextSong());
        shuffleBtn.addActionListener(e -> toggleShuffle());
        repeatBtn.addActionListener(e -> toggleRepeat());
        
        playbackPanel.add(prevBtn);
        playbackPanel.add(playPauseBtn);
        playbackPanel.add(stopBtn);
        playbackPanel.add(nextBtn);
        playbackPanel.add(shuffleBtn);
        playbackPanel.add(repeatBtn);
        
        //======= Progress bar
        progressSlider = new JSlider(0, 100, 0);
        progressSlider.addChangeListener(e -> {
            if (!updatingProgress && progressSlider.getValueIsAdjusting()) {
                long duration = SongPlayer.getDuration();
                long newPosition = (long) (duration * (progressSlider.getValue() / 100.0));
                SongPlayer.setPosition(newPosition);
            }
        });
        
        //======= Volume control
        JPanel volumePanel = new JPanel(new FlowLayout());
        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.addChangeListener(e -> {
            float volume = volumeSlider.getValue() / 100.0f;
            SongPlayer.setVolume(volume);
        });
        volumePanel.add(new JLabel("🔊"));
        volumePanel.add(volumeSlider);
        
        //========= Time label
        timeLabel = new JLabel("00:00 / 00:00");
        
        controlsPanel.add(playbackPanel, BorderLayout.NORTH);
        controlsPanel.add(progressSlider, BorderLayout.CENTER);
        
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.add(timeLabel, BorderLayout.WEST);
        bottomRow.add(volumePanel, BorderLayout.EAST);
        controlsPanel.add(bottomRow, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(nowPlayingPanel, BorderLayout.CENTER);
        add(controlsPanel, BorderLayout.SOUTH);
        
        //======== Initially disable controls
        setControlsEnabled(false);
    }
    
    private void setupNowPlayingPanel() {
        nowPlayingPanel = new JPanel(new BorderLayout());
        nowPlayingPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        nowPlayingLabel = new JLabel("Select a mood to start playing music", SwingConstants.CENTER);
        nowPlayingLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        nowPlayingPanel.add(nowPlayingLabel, BorderLayout.CENTER);
    }
    
    private void setupKeyboardShortcuts() {
        //======== Global key bindings
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "playPause");
        getRootPane().getActionMap().put("playPause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePlayPause();
            }
        });
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "stop");
        getRootPane().getActionMap().put("stop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopPlayback();
            }
        });
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "previous");
        getRootPane().getActionMap().put("previous", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previousSong();
            }
        });
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "next");
        getRootPane().getActionMap().put("next", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextSong();
            }
        });
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "volumeUp");
        getRootPane().getActionMap().put("volumeUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int vol = Math.min(100, volumeSlider.getValue() + 5);
                volumeSlider.setValue(vol);
            }
        });
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "volumeDown");
        getRootPane().getActionMap().put("volumeDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int vol = Math.max(0, volumeSlider.getValue() - 5);
                volumeSlider.setValue(vol);
            }
        });
    }
    
    private void loadMoodSongs() {
        String mood = (String) moodDropdown.getSelectedItem();
        if (mood != null) {
            currentMoodSongs = repository.getSongsForMood(mood);
            if (!currentMoodSongs.isEmpty()) {
                currentSongIndex = 0;
                setControlsEnabled(true);
                playSongAtIndex(0);
            }
        }
    }
    
    private void playSongAtIndex(int index) {
        if (currentMoodSongs == null || currentMoodSongs.isEmpty()) return;
        
        currentSongIndex = index;
        currentSong = currentMoodSongs.get(currentSongIndex);
        SongPlayer.play(currentSong.getFilePath());
        updateNowPlayingDisplay();
    }
    
    private void togglePlayPause() {
        if (SongPlayer.isPlaying()) {
            SongPlayer.pause();
        } else if (SongPlayer.isPaused()) {
            SongPlayer.resume();
        } else if (currentSong != null) {
            SongPlayer.play(currentSong.getFilePath());
        }
    }
    
    private void stopPlayback() {
        SongPlayer.stop();
    }
    
    private void nextSong() {
        if (currentMoodSongs == null || currentMoodSongs.isEmpty()) return;
        
        if (shuffleMode) {
            currentSongIndex = ThreadLocalRandom.current().nextInt(currentMoodSongs.size());
        } else {
            currentSongIndex = (currentSongIndex + 1) % currentMoodSongs.size();
        }
        playSongAtIndex(currentSongIndex);
    }
    
    private void previousSong() {
        if (currentMoodSongs == null || currentMoodSongs.isEmpty()) return;
        
        if (shuffleMode) {
            currentSongIndex = ThreadLocalRandom.current().nextInt(currentMoodSongs.size());
        } else {
            currentSongIndex = (currentSongIndex - 1 + currentMoodSongs.size()) % currentMoodSongs.size();
        }
        playSongAtIndex(currentSongIndex);
    }
    
    private void toggleShuffle() {
        shuffleMode = !shuffleMode;
        shuffleBtn.setBackground(shuffleMode ? Color.CYAN : null);
        shuffleBtn.setOpaque(shuffleMode);
    }
    
    private void toggleRepeat() {
        repeatMode = !repeatMode;
        repeatBtn.setBackground(repeatMode ? Color.CYAN : null);
        repeatBtn.setOpaque(repeatMode);
    }
    
    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        applyTheme();
    }
    
    private void applyTheme() {
        Color bg = isDarkTheme ? darkBg : lightBg;
        Color fg = isDarkTheme ? darkFg : lightFg;
        Color panelBg = isDarkTheme ? darkPanel : lightPanel;
        
        getContentPane().setBackground(bg);
        setComponentTheme(this, bg, fg, panelBg);
        
        themeToggleBtn.setText(isDarkTheme ? "☀" : "🌙");
        repaint();
    }
    
    private void setComponentTheme(Container container, Color bg, Color fg, Color panelBg) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(panelBg);
                setComponentTheme((Container) comp, bg, fg, panelBg);
            } else if (comp instanceof JLabel) {
                comp.setForeground(fg);
                comp.setBackground(bg);
            } else if (comp instanceof JButton) {
                comp.setBackground(panelBg);
                comp.setForeground(fg);
            } else if (comp instanceof JComboBox) {
                comp.setBackground(panelBg);
                comp.setForeground(fg);
            }
            if (comp instanceof Container) {
                setComponentTheme((Container) comp, bg, fg, panelBg);
            }
        }
    }
    
    private void updateNowPlayingDisplay() {
        if (currentSong != null) {
            String mood = (String) moodDropdown.getSelectedItem();
            nowPlayingLabel.setText("<html><center><b>" + currentSong.getTitle() + "</b><br/>" +
                                  "Mood: " + mood + "<br/>" +
                                  "Track " + (currentSongIndex + 1) + " of " + currentMoodSongs.size() +
                                  "</center></html>");
        }
    }
    
    private void setControlsEnabled(boolean enabled) {
        playPauseBtn.setEnabled(enabled);
        stopBtn.setEnabled(enabled);
        prevBtn.setEnabled(enabled);
        nextBtn.setEnabled(enabled);
        shuffleBtn.setEnabled(enabled);
        repeatBtn.setEnabled(enabled);
        progressSlider.setEnabled(enabled);
    }
    
    private String formatTime(long microseconds) {
        long seconds = microseconds / 1_000_000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    //======== PlaybackListener implementation
    @Override
    public void onPositionChanged(long position, long duration) {
        SwingUtilities.invokeLater(() -> {
            updatingProgress = true;
            if (duration > 0) {
                int progress = (int) ((position * 100) / duration);
                progressSlider.setValue(progress);
            }
            timeLabel.setText(formatTime(position) + " / " + formatTime(duration));
            updatingProgress = false;
        });
    }
    
    @Override
    public void onPlaybackStopped() {
        SwingUtilities.invokeLater(() -> {
            playPauseBtn.setText("▶");
            progressSlider.setValue(0);
            timeLabel.setText("00:00 / 00:00");
            
            //========= Auto-play next song if repeat mode is on
            if (repeatMode && currentMoodSongs != null && !currentMoodSongs.isEmpty()) {
                nextSong();
            }
        });
    }
    
    @Override
    public void onPlaybackStarted() {
        SwingUtilities.invokeLater(() -> {
            playPauseBtn.setText("⏸");
        });
    }
}