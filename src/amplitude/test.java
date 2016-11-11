package amplitude;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

public class test {
	public static void main(String[] args) {
		File file = new File("/Users/Ralla/Documents/workspace/amplitude/data_test1.wav");
		//File file = new File("/Users/Ralla/Documents/workspace/amplitude/10kHz_44100Hz_16bit_05sec.wav");
		WaveFile wav;
		long sum=0;
		int count =0;
		double dAvg;
		   double preAvg =0;
		    double differance=0;
		
		try {
			wav = new WaveFile(file);
			//int amplitudeExample = wav.getSampleInt(140); // 140th amplitude value.
			//System.out.println("amplitude:"+ amplitudeExample);
			System.out.println("getFrameCount:"+ wav.getFramesCount());
			for (int i = 0; i < wav.getFramesCount(); i++) {
			    int amplitude = wav.getSampleInt(i);
			    count++;
			 
			    
			    //System.out.println("second:" + SamplesToSeconds(i,wav.getSampleRate())+"  amp:"+amplitude);
			    
			    
			    
			   sum += amplitude;
			   if(count>=wav.getSampleRate()){
				   //System.out.println("C:"+count);
				   dAvg = sum/count;
				   float time = SamplesToSeconds(i,wav.getSampleRate());
			 // System.out.println("second:" + time+"amp:"+dAvg);
			  int t=(int)time;
			/*  for (int a=t; a< t*48000;a++){
				  int amp = wav.getSampleInt(i);
				  
				  sumMeanSquare = sumMeanSquare + Math.pow(amp - dAvg, 2d);
			  }
			  
			  double averageMeanSquare = sumMeanSquare/count;
			  int answer = (int) (Math.pow(averageMeanSquare, 0.5d)+0.5);
			 
			  System.out.println("second:" + time+"RMS:"+answer);
			  sum =0;
			  averageMeanSquare=0;
			  sumMeanSquare=0;
			  dAvg=0;
			  count=0;
			  time=0;
			   }*/
			  differance = Math.abs(dAvg-preAvg); 
			  System.out.println("second:" + time+"  amp:"+dAvg +" diff: "+differance);
			  if(differance>=1760)
				  System.out.println("This is the seconds : "+time);
			  preAvg=dAvg;
			  dAvg =0 ; 
			  sum=0;
			  count =0; 
			 
			  
			}
			   
			}
			System.out.println(wav.isCanPlay());
			wav.play();
			
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	public static float SamplesToSeconds(int samples,int sampleRate)
    {
        return samples / (sampleRate);
    }

}
