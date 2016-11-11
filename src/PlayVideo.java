import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    private InputStream inputStream;
    private BufferedImage img;
    private byte[] bytes;
    private PlaySound playSound;
    private String videoFileName;
    private int width = 480;
    private int height = 270;
    private double fps = 30;
    private long numberOfPixelsPerFrame = width * height * 3;
    private long numberOfFrames;
    double numberOfSamplesPerFrame;

    public boolean running = false;
    public int targetFps = 30;

    @Override
    public void run() {
        playVideo();
        System.out.println("test");
    }

    public PlayVideo(String videoFilename, PlaySound playSound) {
        this.videoFileName = videoFilename;
        this.playSound = playSound;
    }

    private void playVideo() {
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {
            File file = new File(videoFileName);
            inputStream = new FileInputStream(file);

            numberOfFrames = file.length()/numberOfPixelsPerFrame;

            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle("MyPlayer");
            frame.setSize(width,height+100);

            bytes = new byte[(int)numberOfPixelsPerFrame];

            PlayVideoComponent component = new PlayVideoComponent();

            // Number of Audio Samples Per Video Frame
            numberOfSamplesPerFrame = playSound.getSampleRate()/fps;
            System.out.println(numberOfSamplesPerFrame);


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



            /*

            // Video Frame offsets to sync audio and video
            int offset = 0;

            // Audio ahead of video, roll video forward to catch up
            int j=0;
            while(j<Math.round(playSound.getPosition()/numberOfSamplesPerFrame)) {
                System.out.println("1: video < audio => FF video");
                readBytes();
                component.setImg(img);
                frame.add(component);
                frame.repaint();
                frame.setVisible(true);
                j++;
            }
            // Video ahead of audio, wait for audio to catch up
            while(j>Math.round(offset+playSound.getPosition()/numberOfSamplesPerFrame)) {
                System.out.println("2: video > audio => pause video");
            }

            for(int i=j;i<numberOfFrames;i++) {
                // Video ahead of audio, wait for audio to catch up
                while(i>Math.round(offset+playSound.getPosition()/numberOfSamplesPerFrame)) {
                     // System.out.println("3: video > audio => pause video");
                     // System.out.println(i);
                }

                // Audio ahead of video, roll video forward to catch up
                while(i<Math.round(playSound.getPosition()/numberOfSamplesPerFrame)) {
                    System.out.println("4: video < audio => FF video");
                    readBytes();
                    component.setImg(img);
                    frame.add(component);
                    frame.repaint();
                    frame.setVisible(true);
                    i++;
                }

                readBytes();
                component.setImg(img);
                frame.add(component);
                frame.repaint();
                frame.setVisible(true);
            }
            */
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Reads in the bytes of raw RGB data for a frame.
     */
    private  void readBytes() {
        // System.out.println(numberOfSamplesPerFrame);
        System.out.println(playSound.getPosition());
        // System.out.println(playSound.getSampleRate());
        // Prints frame number every second if necessary.


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
}

