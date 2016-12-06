import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Youngmin on 2016. 11. 10..
 */
public class MyPlayer {

    static JFrame frame;

    public static void StartThread(String videoName, String audioName) {
        String videoFilename = videoName;
        String audioFilename = audioName;

        // opens the inputStream
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(audioFilename);
            // inputStream = this.getClass().getResourceAsStream(filename);

            // initializes the playSound Object
            PlaySound playSound = new PlaySound(inputStream);
            PlayVideo playVideo = new PlayVideo(videoFilename, playSound);

            Thread t1 = new Thread(playSound);
            Thread t2 = new Thread(playVideo);

            t1.start();
            Thread.sleep(200); // since video is little faster than audio, tried to make sync.
            t2.start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("MyPlayer");
        frame.setSize(600, 400);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setPreferredSize(new Dimension(100, 100));
        frame.getContentPane().add(buttonPanel, BorderLayout.EAST);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing

        MyButton playButton = new MyButton("Play");
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(playButton);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing

        MyButton pauseButton = new MyButton("Pause");
        pauseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(pauseButton);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing

        MyButton stopButton = new MyButton("Stop");
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(stopButton);

        buttonPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacing

        frame.setVisible(true);

        StartThread(args[0], args[1]);
    }
}
