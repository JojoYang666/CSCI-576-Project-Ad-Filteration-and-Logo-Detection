

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MyProgram{
	private static ArrayList<Shot> shots;
	private double globalYMean;
	private double globalMseAmp;
	private static final int width = 480, height = 270;
	private ArrayList<Scene> scenes;
	private static String inputVideoFile, inputAudioFile, outputVideoFile, outputAudioFile;
	private static double currentShotLumTotal;
	private static double[][] currentYMatrix = new double[width][height];
	private static double[][] prevYMatrix = new double[width][height];
	private static double[][] currentUMatrix = new double[width][height];
	private static double[][] prevUMatrix = new double[width][height];
	private static double[][] currentVMatrix = new double[width][height];
	private static double[][] prevVMatrix = new double[width][height];
	private static YUV currentYUV;

	
	public static void main(String[] args){
		parseArgs(args);
		FrameReader fReader = new FrameReader(inputVideoFile, width, height);
		makeShots(fReader);
		System.out.println("SIZE OF FILE - " + fReader.getFileLength());
		System.out.println("SIZE OF SHOTS - " + shots.size());
		for(Shot s: shots){
			System.out.println("SHOT: " + s.getStartingByte() + "   " + s.getLengthOfShot());
		}
		//for(Shot s: shots){
		//	System.out.println(s.getStartingByte() + " " + s.getLengthOfShot());
		//}
		writeToDisk();
		//makeScenes();
		fReader.close();
	}
	/**
	 * Making Shot object using the frames from Frame Reader
	 * @param fReader
	 */
	private static void makeShots(FrameReader fReader){
		long maxNumOfFrames = fReader.getNumberOfFrames();
		long offset = 0, numOfFrames = 0;
		double yFrameAvg = 0;
		double interFrameDiffEstimateY = 0, interFrameDiffEstimateU = 0, interFrameDiffEstimateV = 0;
		double shotBasedDiffEstimateY = 0, shotBasedDiffEstimateU = 0, shotBasedDiffEstimateV = 0;
		boolean newShot = true, firstFrameDiffEstimate = true;					
		shots = new ArrayList<Shot>();
		
		System.out.println("MAX NUMBER OF FRAMES - " + maxNumOfFrames);

		//TODO Audio processing
		while(offset<maxNumOfFrames){
			//System.out.println("CURRENT FRAME - " + offset);
			if(newShot==false){
                currentYUV = fReader.read();
				currentYMatrix = currentYUV.getY();
				currentUMatrix = currentYUV.getU();
				currentVMatrix = currentYUV.getV();
				yFrameAvg = getFrameAvg();
				numOfFrames++;
				interFrameDiffEstimateY = getFrameDifference(currentYMatrix, prevYMatrix);
				interFrameDiffEstimateU = getFrameDifference(currentUMatrix, prevUMatrix);
				interFrameDiffEstimateV = getFrameDifference(currentVMatrix, prevVMatrix);
				
				if(firstFrameDiffEstimate==false){
					if(((interFrameDiffEstimateY*0.1 + interFrameDiffEstimateU*0.45 + interFrameDiffEstimateV*0.45) - (shotBasedDiffEstimateY/(numOfFrames-2)*0.1 + shotBasedDiffEstimateU/(numOfFrames-2)*0.45 + shotBasedDiffEstimateV/(numOfFrames-2)*0.45))>(0.2*(shotBasedDiffEstimateY/(numOfFrames-2)*0.1 + shotBasedDiffEstimateU/(numOfFrames-2)*0.45 + shotBasedDiffEstimateV/(numOfFrames-2)*0.45))){
						//Exceeded threshold
						newShot = true;
						firstFrameDiffEstimate = true;
						shots.get(shots.size()-1).setLengthOfShot((offset*fReader.getLen()) - shots.get(shots.size()-1).getStartingByte());
						shots.get(shots.size()-1).setyMean(currentShotLumTotal/(numOfFrames-1));
						currentShotLumTotal = 0;
						shotBasedDiffEstimateY = 0;
						shotBasedDiffEstimateU = 0;
						shotBasedDiffEstimateV = 0;
						numOfFrames = 0;
						//System.out.println(shots.get(shots.size()-1).getyMean());
					}
					else{
						currentShotLumTotal += yFrameAvg;
						offset+=1;
					}
				}
				else if(firstFrameDiffEstimate==true){
					currentShotLumTotal += yFrameAvg;
					offset += 1;
					firstFrameDiffEstimate = false;
				}
				shotBasedDiffEstimateY += interFrameDiffEstimateY;
				shotBasedDiffEstimateU += interFrameDiffEstimateU;
				shotBasedDiffEstimateV += interFrameDiffEstimateV;
			}
			
			if(newShot==true){
				shots.add(new Shot());
				shots.get(shots.size()-1).setStartingByte(offset*fReader.getLen());
				currentYUV = fReader.read();
				currentYMatrix = currentYUV.getY();
				currentUMatrix = currentYUV.getU();
				currentVMatrix = currentYUV.getV();
				yFrameAvg = getFrameAvg();
				numOfFrames++;
				currentShotLumTotal = yFrameAvg;
				offset+=1;
				newShot = false;
			}

			for(int x = 0; x<width; x++){
				for(int y = 0; y<height; y++){
					prevYMatrix[x][y] = currentYMatrix[x][y];
					prevUMatrix[x][y] = currentUMatrix[x][y];
					prevVMatrix[x][y] = currentVMatrix[x][y];
				}
			}
		}

		if(numOfFrames!=0){
			shots.get(shots.size()-1).setLengthOfShot(fReader.getFileLength() - shots.get(shots.size()-1).getStartingByte());
			shots.get(shots.size()-1).setyMean(currentShotLumTotal/(numOfFrames-1));
		}
	}
	
	private static void writeToDisk(){
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
		
		for(Shot s: shots){
			try  
			{
				FileOutputStream fw = new FileOutputStream(new File(fileName + fileNo + extension));
                long bytesRemaining = s.getLengthOfShot();
                byte[] bytes;
                while (bytesRemaining != 0){
                    if (bytesRemaining >= MAX_LIMIT){
                        bytes = new byte[MAX_LIMIT];
                    }
                    else{
                        bytes = new byte[(int)bytesRemaining];
                    }
                    inputFileStream.read(bytes);
                    fw.write(bytes);
                    bytesRemaining -= bytes.length;
                }
			    fw.close();   
			    fileNo++;
			}
			catch (IOException e)
			{
			    System.err.println("Error: " + e.getMessage());
			}
		}
	}
	
	private static void makeScenes(){
		
	}
	
	private static double getFrameAvg(){
		double frameAvg = 0;
		for(int y = 0;y<width;y++){
			for(int x = 0; x<height; x++){
				frameAvg += currentYMatrix[y][x]; 
			}
		}
		return frameAvg/(width*height);
	}

	private static double getFrameDifference(double[][] currentFrameLum, double[][] prevFrameLum){
		double frameDiff = 0;
		for(int x = 0; x<width; x++){
			for(int y = 0; y<height; y++){
				frameDiff += Math.pow(currentFrameLum[x][y] - prevFrameLum[x][y],2);
			}
		}
		return frameDiff/(width*height);
	}
	
	private static void parseArgs(String[] args){
		if(args.length!=1){
			Utilities.die("Not enough arguments! \nUsage java MyProgram input.rgb input.wav output.rgb output.wav");		
		}
		
		inputVideoFile = args[0];
		//inputAudioFile = args[1];
		//outputVideoFile = args[2];
		//outputAudioFile = args[3];
	}
	
}