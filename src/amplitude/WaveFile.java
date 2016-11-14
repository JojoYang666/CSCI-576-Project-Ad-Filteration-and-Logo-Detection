package amplitude;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WaveFile {
	    public final int NOT_SPECIFIED = AudioSystem.NOT_SPECIFIED; // -1
	    public final int INT_SIZE = 4;

	    private int sampleSize = NOT_SPECIFIED;
	    private long framesCount = NOT_SPECIFIED;
	    private int sampleRate = NOT_SPECIFIED;
	    private int channelsNum;
	    private byte[] data;      // wav bytes
	    private AudioInputStream ais;
	    private AudioFormat af;

	    private Clip clip;
	    private boolean canPlay;
	 
	    public WaveFile(File file) throws UnsupportedAudioFileException, IOException {
	        if (!file.exists()) {
	            throw new FileNotFoundException(file.getAbsolutePath());
	        }

	        ais = AudioSystem.getAudioInputStream(file);
	        framesCount = ais.getFrameLength();
	        af = ais.getFormat();

	        sampleRate = (int) af.getSampleRate();
	        

	        sampleSize = af.getSampleSizeInBits() / 8;

	        channelsNum = af.getChannels();

	        long dataLength = framesCount * af.getSampleSizeInBits() * af.getChannels() / 8;
	        
	        System.out.println("sampleRate:"+sampleRate+" sampleSize: "+sampleSize);
	     
	        data = new byte[(int) dataLength];
	        System.out.println("dataLength:"+dataLength+ " data list length:"+data.length);
	        ais.read(data);

	        
	    }

	    public boolean isCanPlay() {
	        return canPlay;
	    }

	  

	    public AudioFormat getAudioFormat() {
	        return af;
	    }

	    public int getSampleSize() {
	        return sampleSize;
	    }

	    public double getDurationTime() {
	        return getFramesCount() / getAudioFormat().getFrameRate();
	    }

	    public long getFramesCount() {
	        return framesCount;
	    }


	    /**
	     * Returns amplitude value
	     */
	    public int getSampleInt(int sampleNumber) {

	        if (sampleNumber < 0 || sampleNumber >= data.length / sampleSize) {
	            throw new IllegalArgumentException(
	                    "sample number can't be < 0 or >= data.length/"
	                            + sampleSize);
	        }

	        byte[] sampleBytes = new byte[4]; //4byte = int

	        for (int i = 0; i < sampleSize; i++) {
	            sampleBytes[i] = data[sampleNumber * sampleSize * channelsNum + i];
	        }

	        int sample = ByteBuffer.wrap(sampleBytes)
	                .order(ByteOrder.LITTLE_ENDIAN).getInt();
	        return sample;
	    }

	    public int getSampleRate() {
	        return sampleRate;
	    }

	    public Clip getClip() {
	        return clip;
	    }
	}

