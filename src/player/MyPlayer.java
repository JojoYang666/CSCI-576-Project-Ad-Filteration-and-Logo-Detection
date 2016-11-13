import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Youngmin on 2016. 11. 10..
 */
public class MyPlayer {

    public static void StartThread() {
        String videoFilename = "data_test1.rgb";
        String audioFilename = "data_test1.wav";

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
            t2.start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void main(String[] args) {
        StartThread();
    }
}
