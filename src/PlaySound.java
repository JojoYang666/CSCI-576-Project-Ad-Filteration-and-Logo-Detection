import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;

/**
 * 
 * PlaySound class uses runnable interface to use thread
 * The purpose of PlaySound class
 *
 * @author Youngmin Shin
 */
public class PlaySound implements Runnable {

	private InputStream waveStream;
	private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
	private static SourceDataLine dataLine;
	private AudioFormat audioFormat;
	private static Clip clip;

	/**
	 * CONSTRUCTOR
	 */
	public PlaySound(InputStream waveStream) {
		this.waveStream = waveStream;
	}

	@Override
	public void run() {
		try {
			this.play();
		} catch (PlayWaveException e) {
			e.printStackTrace();
			return;
		}
	}

    public void play() throws PlayWaveException {

		AudioInputStream audioInputStream = null;
		try {
			//audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);
			//add buffer for mark/reset support, modified by Jian
			InputStream bufferedIn = new BufferedInputStream(this.waveStream);
			audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);

			try {
				// Using Clip instead of dataLine
				clip = AudioSystem.getClip();
				clip.open(audioInputStream);
				clip.start();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}

		} catch (UnsupportedAudioFileException e1) {
			throw new PlayWaveException(e1);
		} catch (IOException e1) {
			throw new PlayWaveException(e1);
		}

		// Obtain the information about the AudioInputStream
		audioFormat = audioInputStream.getFormat();




		/*
		Info info = new Info(SourceDataLine.class, audioFormat);

		// opens the audio channel
		dataLine = null;
		try {
			dataLine = (SourceDataLine) AudioSystem.getLine(info);
			dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
		} catch (LineUnavailableException e1) {
			throw new PlayWaveException(e1);
		}

		// Starts the music :P
		// dataLine.start();

		int readBytes = 0;
		byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];

		try {
			while (readBytes != -1) {
				readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
				if (readBytes >= 0) {
					dataLine.write(audioBuffer, 0, readBytes);
				}
			}
		} catch (IOException e1) {
			throw new PlayWaveException(e1);
		} finally {
			// plays what's left and and closes the audioChannel
			dataLine.drain();
			dataLine.close();
		}
		*/



	}
	public long getPosition() {
		return clip.getLongFramePosition();
	}

	public float getSampleRate() {
		return audioFormat.getFrameRate();
	}
	public static void suspend() {
		clip.stop(); // pause audio
		System.out.println("pause audio");
	}

	public static void resume() {
		clip.start();
		System.out.println("resume audio!");
	}

	public static void stop() {
		clip.stop(); // pause audio
		clip.setFramePosition(0); // Frame position to 0
		System.exit(0); // exit program
		System.out.println("stop audio!");
	}
}
