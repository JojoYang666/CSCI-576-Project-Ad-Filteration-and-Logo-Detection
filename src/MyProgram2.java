import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import amplitude.*;

public class MyProgram2 {
	private static ArrayList<Shot> shots;
	private static final int width = 480, height = 270;
	private ArrayList<Scene> scenes;
	private static ArrayList<Double> valsY = new ArrayList<Double>();
	private static ArrayList<Double> valsU = new ArrayList<Double>();
	private static ArrayList<Double> valsV = new ArrayList<Double>();
	private static String inputVideoFile, inputAudioFile, outputVideoFile, outputAudioFile;
	private static int sizeOfSlidingWindow = 5;
	private static double[][] currentYMatrix = new double[width][height];
	private static double[][] prevYMatrix = new double[width][height];
	private static double[][] currentUMatrix = new double[width][height];
	private static double[][] prevUMatrix = new double[width][height];
	private static double[][] currentVMatrix = new double[width][height];
	private static double[][] prevVMatrix = new double[width][height];
	private static YUV currentYUV;
	private static double threshold;
	
	public static void main(String[] args) {
		parseArgs(args);
		Test audioReaderTest = new Test();
		audioReaderTest.openAudioFile(inputAudioFile);
		
		FrameReader fReader = new FrameReader(inputVideoFile, width, height);
		makeShots(fReader);
		System.out.println("SIZE OF FILE - " + fReader.getFileLength());
		System.out.println("SIZE OF SHOTS - " + shots.size());
		int index = 1;
		double sDev = 0.0;
		for (Shot s : shots) {
			System.out.println(index + ". SHOT: " + s.getStartingByte() + "   " + s.getLengthOfShot());
//			s.setRmsMean(audioReaderTest.calcRms(s.getStartingByte(), s.getLengthOfShot(), 480*270*3));
			sDev = audioReaderTest.calcStandardDeviationOfAmplitudes(s.getStartingByte(), s.getLengthOfShot(), 480*270*3);
//			System.out.println("RMS OF SHOT - " + s.getRmsMean());
			System.out.println("STANDARD DEVIATION OF SHOT: " + sDev);
			s.setWeight(s.getLengthOfShot()/(double)fReader.getFileLength());
			System.out.println("WEIGHT OF SHOT - " + s.getWeight() + "\n\n");
			index++;
		}
		//writeToDisk();
		//makeScenes();
		
//		threshold = 0.0;
//		double rmsMean = 0.0, wMean = 0.0, deviation = 0.0;
//		for (Shot s : shots){
//			rmsMean += s.getRmsMean();
//			wMean += s.getWeight();
//		}
//		rmsMean /= shots.size();
//		wMean /= shots.size();
//		for (Shot s : shots){
//			deviation += Math.abs((wMean*rmsMean) - (s.getWeight()*s.getRmsMean()));
//		}
//		deviation/= shots.size();
//		threshold = wMean*rmsMean;
//		System.out.println("DEVIATION - " + deviation);
//		System.out.println("THREASHOLD - " + threshold);
//		System.out.println("RMS MEAN - " + rmsMean);
//		System.out.println("WMEAN - " + wMean + "\n\n******************\n\n");
		
//		for (Shot s: shots){
//			if(((threshold - deviation) > (s.getRmsMean()*s.getWeight()))||((s.getRmsMean()*s.getWeight())>(threshold + deviation))){
//				s.setAd(true);
//			}
//		}
//		
//		index = 1;
//		for (Shot s : shots) {
//			System.out.println(index + ". SHOT: " + s.getStartingByte() + "   " + s.getLengthOfShot());
//			System.out.println("IS AD? - " + s.isAd());
//			index++;
//		}
		
		fReader.close();
	}

	/**
	 * Making Shot object using the frames from Frame Reader
	 * 
	 * @param fReader
	 */
	private static void makeShots(FrameReader fReader) {
		long maxNumOfFrames = fReader.getNumberOfFrames(), offset = 0;
		double interFrameDiffEstimateY = 0, interFrameDiffEstimateU = 0, interFrameDiffEstimateV = 0;
		double yThreshold = 0, uThreshold = 0, vThreshold = 0;
		int votes = 0;
		boolean startProcess = true, firstFrameDiffEstimate = true;
		shots = new ArrayList<Shot>();

		System.out.println("MAX NUMBER OF FRAMES - " + maxNumOfFrames);

		// TODO Audio processing
		while (offset < maxNumOfFrames) {
			System.out.println("CURRENT FRAME - " + offset );
			if (startProcess == false) {
				currentYUV = fReader.read();
				currentYMatrix = currentYUV.getY();
				currentUMatrix = currentYUV.getU();
				currentVMatrix = currentYUV.getV();
				interFrameDiffEstimateY = blockMatchingAlgo(currentYMatrix, prevYMatrix);
				interFrameDiffEstimateU = blockMatchingAlgo(currentUMatrix, prevUMatrix);
				interFrameDiffEstimateV = blockMatchingAlgo(currentVMatrix, prevVMatrix);

				if(valsY.size()==sizeOfSlidingWindow)
					valsY.remove(0);
				if(valsU.size()==sizeOfSlidingWindow)
					valsU.remove(0);
				if(valsV.size()==sizeOfSlidingWindow)
					valsV.remove(0);				
				
				if(firstFrameDiffEstimate==true){
					firstFrameDiffEstimate = false;
					valsY.add((double) interFrameDiffEstimateY);
					valsU.add((double) interFrameDiffEstimateU);
					valsV.add((double) interFrameDiffEstimateV);
				}
				else{
					votes = 0;
					if(interFrameDiffEstimateY>yThreshold)
						votes++;
					if(interFrameDiffEstimateU>uThreshold)
						votes++;
					if(interFrameDiffEstimateV>vThreshold)
						votes++;
					if(votes>=2){
//						System.out.println("THAT'S A SHOT, BOYS! " + offset);
						shots.get(shots.size() - 1).setLengthOfShot((offset * fReader.getLen()) - shots.get(shots.size() - 1).getStartingByte());
//						System.out.println(shots.get(shots.size()-1).getStartingByte() + " " + offset*fReader.getLen() + " " + shots.get(shots.size()-1).getLengthOfShot());
						shots.add(new Shot());
						shots.get(shots.size()-1).setStartingByte(offset*fReader.getLen());
						firstFrameDiffEstimate = true;
						valsY.clear();
						valsU.clear();
						valsV.clear();
					}
					else{
						valsY.add((double) interFrameDiffEstimateY);
						valsU.add((double) interFrameDiffEstimateU);
						valsV.add((double) interFrameDiffEstimateV);
					}
				}
				
				yThreshold = 2*calcThreshold(valsY);
				uThreshold = 2*calcThreshold(valsU);
				vThreshold = 2*calcThreshold(valsV);
				offset += 1;
			}

			if (startProcess == true) {
				System.out.println("Starting with " + offset);
				shots.add(new Shot());
				shots.get(shots.size() - 1).setStartingByte(offset * fReader.getLen());
				currentYUV = fReader.read();
				currentYMatrix = currentYUV.getY();
				currentUMatrix = currentYUV.getU();
				currentVMatrix = currentYUV.getV();
				valsY.clear();
				valsU.clear();
				valsV.clear();
				offset += 1;
				startProcess = false;
			}

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					prevYMatrix[x][y] = currentYMatrix[x][y];
					prevUMatrix[x][y] = currentUMatrix[x][y];
					prevVMatrix[x][y] = currentVMatrix[x][y];
				}
			}
		}

		shots.get(shots.size() - 1).setLengthOfShot(fReader.getFileLength() - shots.get(shots.size() - 1).getStartingByte());
	}

	private static double calcThreshold(ArrayList<Double> vals){
		double avg = 0, sumOfDiff = 0;
		for(int i = 0; i<vals.size();i++){
			avg += vals.get(i);
		}
		avg /= vals.size();
		
		for(int i = 0; i<vals.size(); i++){
			sumOfDiff += Math.abs(vals.get(i) - avg);
		}
		sumOfDiff /= vals.size();
		return (avg + sumOfDiff);
	}
	
	private static void writeToDisk() {
		final int MAX_LIMIT = 999999999;
		String fileName = "output";
		int fileNo = 1;
		String extension = ".rgb";
		InputStream inputFileStream = null;

		try {
			File file = new File(inputVideoFile);
			inputFileStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			Utilities.die("File not found - " + inputVideoFile);
		}

		for (Shot s : shots) {
			try {
				FileOutputStream fw = new FileOutputStream(new File(fileName + fileNo + extension));
				long bytesRemaining = s.getLengthOfShot();
				byte[] bytes;
				while (bytesRemaining != 0) {
					if (bytesRemaining >= MAX_LIMIT) {
						bytes = new byte[MAX_LIMIT];
					} else {
						bytes = new byte[(int) bytesRemaining];
					}
					inputFileStream.read(bytes);
					fw.write(bytes);
					bytesRemaining -= bytes.length;
				}
				fw.close();
				fileNo++;
			} catch (IOException e) {
				System.err.println("Error: " + e.getMessage());
			}
		}
	}

	private static double blockMatchingAlgo(double[][] currentFrame, double[][] prevFrame){
		double frameDiff = 0;
		double blockDiff = 0;
		double macroblockDiff = 0;
		int stepSize, posX, posY, startX, startY;
		
		for(int x = 0; x<(width-16); x+=16){
			for(int y = 0; y<(height-16); y+=16){
				macroblockDiff = Double.MAX_VALUE;
				stepSize = 4;
				startX = x;
				startY = y;
				posX = x;
				posY = y;
				
				while(stepSize>0){
					startX = posX;
					startY = posY;
					for(int regionX = -stepSize; regionX<=stepSize; regionX+=stepSize){
						if((startX+regionX < 0)|(startX+regionX >= width)|(startX+regionX+16 >= width))
							continue;
						for(int regionY = -stepSize; regionY<=stepSize; regionY+=stepSize){
							if((startY+regionY < 0)|(startY+regionY >= height)|(startY+regionY+16 >= height))
								continue;
							if(regionX==0 && regionY==0)
								continue;
							blockDiff = 0;
							for(int iDisp = 0; iDisp<16; iDisp++){
								for(int jDisp = 0; jDisp<16; jDisp++){
									blockDiff += Math.abs(currentFrame[startX+iDisp][startY+jDisp] - prevFrame[startX+regionX+iDisp][startY+regionY+jDisp]);
								}
							}
							if(macroblockDiff > blockDiff/256){
								posX = startX + regionX;
								posY = startY + regionY;
								macroblockDiff = blockDiff/256;
							}
						}
					}
					stepSize /= 2;
				}
				frameDiff += macroblockDiff;
			}
		}
		return frameDiff;
	}

	private static void parseArgs(String[] args) {
		if (args.length != 2) {
			Utilities.die("Not enough arguments! \nUsage java MyProgram input.rgb input.wav output.rgb output.wav");
		}

		inputVideoFile = args[0];
		inputAudioFile = args[1];
		// outputVideoFile = args[2];
		// outputAudioFile = args[3];
	}

}