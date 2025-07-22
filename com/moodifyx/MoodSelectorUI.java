package com.moodifyx;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.*;


public class MoodSelectorUI  extends JFrame{
    private final MoodRepository repository = new MoodRepository();

    public MoodSelectorUI(){
        setTitle("Moodifyx");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setupUI();
        setVisible(true);
    }

    private void setupUI(){
        Set<String> moods = repository.getAllMoods();
        JComboBox<String> moodDropdown = new JComboBox<>(moods.toArray(new String[0]));
        JButton detectMoodBtn = new JButton("Recommend Music");
        detectMoodBtn.addActionListener( e -> {
            String mood = (String) moodDropdown.getSelectedItem();

            while(true){
                List<Song> songs = repository.getSongsForMood(mood);
                Song selected = songs.get(ThreadLocalRandom.current().nextInt(songs.size()));
                SongPlayer.play(selected.getFilePath());

                Object[] options = {"Another Song", "Change Mood", "Stop Music and Exit"};
                int choice = JOptionPane.showOptionDialog(
                    this,
                    "Now Playing: " + selected.getTitle() + "\nMood: " + mood,
                    "Your Music",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
                );

                SongPlayer.stop();

                if(choice == JOptionPane.YES_OPTION){
                    continue;
                } else if(choice == JOptionPane.NO_OPTION){
                    break;
                } else{
                    System.exit(0);
                }
            }
        });
        JPanel panel = new JPanel();
        panel.add(new JLabel("Select your mood: "));
        panel.add(moodDropdown);
        panel.add(detectMoodBtn);
        add(panel);
    }
} 
