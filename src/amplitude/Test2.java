package amplitude;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

public class Test2 {
	private static WaveFile wav;
	private static long sum = 0;
	private static int count = 0;
	private static double dAvg, totalAvg = 0;
	private static double preAvg = 0;
	private static double differance = 0;
	private static int totalSec = 0;
	
	public void openAudioFile(String fileName) {
		File file = new File(fileName);
		// File file = new
		// File("/Users/Ralla/Documents/workspace/amplitude/10kHz_44100Hz_16bit_05sec.wav");
		
		try {
			wav = new WaveFile(file);
			// int amplitudeExample = wav.getSampleInt(140); // 140th amplitude
			// value.
			// System.out.println("amplitude:"+ amplitudeExample);
			System.out.println("getFrameCount:" + wav.getFramesCount());
			totalSec = (int) SamplesToSeconds(wav.getFramesCount(), wav.getSampleRate());
			System.out.println("getTotalSec:" + totalSec);
			double[] amps = new double[(int) totalSec];
			double[] tmepAmps = new double[(int) totalSec];

			double sumAmps = 0;
			/*for (int i = 0; i < wav.getFramesCount(); i++) {
				int amplitude = wav.getSampleInt(i);
				count++;

				// System.out.println("second:" +
				// SamplesToSeconds(i,wav.getSampleRate())+" amp:"+amplitude);

				sum += amplitude;
				if (count >= wav.getSampleRate()) {
					dAvg = sum / count;
					float time = SamplesToSeconds(i, wav.getSampleRate());

					amps[(int) time] = dAvg;
					sumAmps += dAvg;

					dAvg = 0;
					sum = 0;
					count = 0;

				}

			}
			// Standard diviation
			totalAvg = sumAmps / totalSec;

			double sumOfAveSquare = 0;
			for (int i = 0; i < amps.length; i++) {

				tmepAmps[i] = Math.pow((amps[i] - totalAvg), 2);

				sumOfAveSquare += tmepAmps[i];
			}
			sumOfAveSquare = sumOfAveSquare / tmepAmps.length;
			double sigma = 0;
			sigma = Math.sqrt(sumOfAveSquare);
			System.out.println("This is the Standard diviation" + sigma);

			// printing results.
			for (int i = 0; i < amps.length; i++) {
				// System.out.println("second:" + i+" amp:"+amps[i] );

				if (i != 0 && Math.abs(amps[i] - amps[i - 1]) > sigma) {

					System.out.println("Change in sound more that Sigma at " + i + "sec.");
				}
			}*/
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static float SamplesToSeconds(long l, int sampleRate) {
		return l / (sampleRate);
	}
	
	public double calcRms(long startingByte, long lengthOfShot, long videoFrameLength){
		double peakAmp = 0.0;
		int startingAudioSample = (int)(startingByte/videoFrameLength)*1600;
		int numOfAudioSamples = (int)(lengthOfShot/videoFrameLength)*1600;
		if((startingAudioSample + numOfAudioSamples)>wav.getFramesCount())
			numOfAudioSamples = (int) (wav.getFramesCount() - startingAudioSample);
		System.out.println("STARTING SAMPLE NUMBER - " + startingAudioSample);
		System.out.println("NUMBER OF AUDIO SAMPLES - " + numOfAudioSamples);
		for(int i = startingAudioSample; i<(startingAudioSample + numOfAudioSamples); i++){
			System.out.println("*** " + wav.getSampleInt(i) + " ***");
			peakAmp = Math.max(peakAmp, wav.getSampleInt(i));
		}
		return ((double)peakAmp)*0.707;
	}

}
