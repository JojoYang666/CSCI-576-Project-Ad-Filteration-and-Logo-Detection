import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by Youngmin on 2016. 11. 11..
 *
 * Video Frame Size: 480*270 (width: 480, height: 270)
 * Video Data format: RGB format similar with assignments
 * Video FPS: 30 frames/second
 * Video Number of Pixels per Frame: 480*270*3 = 388800
 * Video Number of Frames per File: 3499200000 / 388000 = 9000
 *
 * Auido Sampling Rate: 48000 HZ
 * Audio Channels : 1 mono
 * Audio Bits per Sample: 16
 * Number of Audio Samples per video Frame: 48000 sample/second / 30 frame/second = 1600
 *
 */
public class PlayVideo implements Runnable {
    private int currentFrame;
    private static final Object lock = new Object();
    private String threadName;
    private static boolean suspended = false;
    private static boolean stop = false;

    private static InputStream inputStream;
    private static BufferedImage img;
    private byte[] bytes;
    private PlaySound playSound;
    private String videoFileName;
    private static int width = 480;
    private static int height = 270;
    private double fps = 30;
    private static long numberOfPixelsPerFrame = width * height * 3;
    private static long numberOfFrames;
    private static double numberOfSamplesPerFrame;

    public boolean running = false;
    public int targetFps = 30;

    public PlayVideo(String videoFilename, PlaySound playSound) {
        this.videoFileName = videoFilename;
        this.playSound = playSound;
    }

    @Override
    public void run() {
        try {
            playVideo();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void playVideo() throws InterruptedException {

        currentFrame=0;

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {
            File file = new File(videoFileName);
            inputStream = new FileInputStream(file);
            numberOfFrames = file.length() / numberOfPixelsPerFrame;
            bytes = new byte[(int) numberOfPixelsPerFrame];

            PlayVideoComponent component = new PlayVideoComponent();

            // Number of Audio Samples Per Video Frame
            numberOfSamplesPerFrame = playSound.getSampleRate() / fps;


            // I don't care about sync. 30fps video player
            /**
            running = true;

            final long OPTIMAL_DURATION = 1000000000 / targetFps; // Expected frame duration in nano seconds
            long startTime = 0;
            while (running)
            {
                System.out.println("Frame duration: " + (System.nanoTime() - startTime) / 1000000.0);
                startTime = System.nanoTime();

                //// Update \\\\

                readBytes();
                component.setImg(img);
                frame.add(component);
                frame.repaint();
                frame.setVisible(true);

                // Sleep if any milliseconds available \\
                while (System.nanoTime() - startTime <= OPTIMAL_DURATION - 4000000) { // 5ms
                    // System.out.println("SLEEP");
                    try { Thread.sleep(0); } catch (Exception e) {}
                }

                // Keep busy waiting for the rest of the time
                while (System.nanoTime() - startTime < OPTIMAL_DURATION) {
                    Thread.yield();
                }
            }
            */


            /**
             *   Sync between audio and video
             */
            // Video Frame offsets to sync audio and video
            int offset = 0;

            // Audio ahead of video, roll video forward to catch up
            int j = 0;
            if (!stop)
            {
                while (j < Math.round(playSound.getPosition() / numberOfSamplesPerFrame)) {
                    // System.out.println("1: video < audio => FF video");
                    readBytes();
                    component.setImg(img);
                    MyPlayer.frame.add(component);
                    MyPlayer.frame.repaint();
                    MyPlayer.frame.setVisible(true);
                    j++;
                }
                // Video ahead of audio, wait for audio to catch up
                while (j > Math.round(offset + playSound.getPosition() / numberOfSamplesPerFrame)) {
                    // System.out.println("2: video > audio => pause video");
                }

                for (int i = j; i < numberOfFrames; i++) {

                    // Video ahead of audio, wait for audio to catch up
                    while (i > Math.round(offset + playSound.getPosition() / numberOfSamplesPerFrame)) {
                        // System.out.println(playSound.getPosition());
                        // System.out.println("3: video > audio => pause video");
                        // System.out.println(i);
                    }

                    // Audio ahead of video, roll video forward to catch up
                    while (i < Math.round(playSound.getPosition() / numberOfSamplesPerFrame)) {
                        // System.out.println("4: video < audio => FF video");
                        readBytes();
                        component.setImg(img);
                        MyPlayer.frame.add(component);
                        MyPlayer.frame.repaint();
                        MyPlayer.frame.setVisible(true);
                        i++;
                    }

                    readBytes();
                    component.setImg(img);
                    MyPlayer.frame.add(component);
                    MyPlayer.frame.repaint();
                    MyPlayer.frame.setVisible(true);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readBytes() {

        currentFrame++;

        // System.out.println(currentFrame);
        // System.out.println(numberOfSamplesPerFrame);
        // System.out.println(playSound.getPosition());
        // System.out.println(playSound.getSampleRate());
        // Prints frame number every second if necessary.

        synchronized (this) {
            while(suspended) {
                Thread.interrupted();
            }
        }

        System.out.println(currentFrame);

        try {
            int offset = 0;
            int numRead = 0;

            while (offset < bytes.length && (numRead=inputStream.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
            int ind = 0;
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    img.setRGB(x,y,pix);
                    ind++;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        System.out.println("stop video");

        try {
            inputStream.close(); // close file input stream
            stop = true; // stop thread
            System.exit(0); // exit program

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void suspend() {
        suspended = true;
        System.out.println("pause video");
    }

    public static void resume() {
        synchronized (lock) {
            suspended = false;
            lock.notify();
            System.out.println("resume video");
        }
    }
}

