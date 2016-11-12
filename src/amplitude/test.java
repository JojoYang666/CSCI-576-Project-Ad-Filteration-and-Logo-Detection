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
		double dAvg, totalAvg =0;
		   double preAvg =0;
		    double differance=0;
		int totalSec=0;
		    
		try {
			wav = new WaveFile(file);
			//int amplitudeExample = wav.getSampleInt(140); // 140th amplitude value.
			//System.out.println("amplitude:"+ amplitudeExample);
			System.out.println("getFrameCount:"+ wav.getFramesCount());
			totalSec=(int)SamplesToSeconds(wav.getFramesCount(),wav.getSampleRate());
			double []amps = new double[(int) totalSec];
			double []tmepAmps = new double [(int)totalSec];
			
			double sumAmps=0;
			for (int i = 0; i < wav.getFramesCount(); i++) {
			    int amplitude = wav.getSampleInt(i);
			    count++;
			 
			    
			    //System.out.println("second:" + SamplesToSeconds(i,wav.getSampleRate())+"  amp:"+amplitude);
			    
			    
			    
			   sum += amplitude;
			   if(count>=wav.getSampleRate()){
					   dAvg = sum/count;
				   float time = SamplesToSeconds(i,wav.getSampleRate());
				   
			
			
			  amps[(int) time]=dAvg;
			  sumAmps+= dAvg;
			 
			   dAvg =0 ; 
			  sum=0;
			  count =0; 
			 
			  
			}
			   
			}
			// Standard diviation 
			totalAvg= sumAmps/totalSec;
			
			double sumOfAveSquare=0;
			for (int i=0; i<amps.length;i++){
				
				tmepAmps[i]=Math.pow((amps[i]-totalAvg),2);
				
				sumOfAveSquare +=tmepAmps[i];
			}
			sumOfAveSquare=sumOfAveSquare/tmepAmps.length; 
			double sigma = 0;
			sigma = Math.sqrt(sumOfAveSquare);
			System.out.println("This is the Standard diviation"+sigma);
	
			
			//printing results. 		
			for(int i =0; i<amps.length;i++){
			//	 System.out.println("second:" + i+"  amp:"+amps[i] );
				 
				if(i!=0&&Math.abs(amps[i]-amps[i-1])>sigma){
					
					System.out.println("Change in sound more that Sigma at "+i+"sec.");
				}
			}
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	public static float SamplesToSeconds(long l,int sampleRate)
    {
        return l / (sampleRate);
    }

}
