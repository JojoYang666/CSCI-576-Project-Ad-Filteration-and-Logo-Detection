
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MyProgram2 {
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

	public static void main(String[] args) {
		parseArgs(args);
		FrameReader fReader = new FrameReader(inputVideoFile, width, height);
		makeShots(fReader);
		System.out.println("SIZE OF FILE - " + fReader.getFileLength());
		System.out.println("SIZE OF SHOTS - " + shots.size());
		for (Shot s : shots) {
			System.out.println("SHOT: " + s.getStartingByte() + "   " + s.getLengthOfShot());
		}
		// for(Shot s: shots){
		// System.out.println(s.getStartingByte() + " " + s.getLengthOfShot());
		// }
		writeToDisk();
		// makeScenes();
		fReader.close();
	}

	/**
	 * Making Shot object using the frames from Frame Reader
	 * 
	 * @param fReader
	 */
	private static void makeShots(FrameReader fReader) {
		long maxNumOfFrames = fReader.getNumberOfFrames();
		int offset = 0, numOfFrames = 0;
		double yFrameAvg = 0;
		double interFrameDiffEstimateY = 0, interFrameDiffEstimateU = 0, interFrameDiffEstimateV = 0;
		double shotBasedDiffEstimateY = 0, shotBasedDiffEstimateU = 0, shotBasedDiffEstimateV = 0;
		boolean newShot = true, firstFrameDiffEstimate = true;
		shots = new ArrayList<Shot>();

		System.out.println("MAX NUMBER OF FRAMES - " + maxNumOfFrames);

		// TODO Audio processing
		while (offset < maxNumOfFrames) {
			System.out.println("CURRENT FRAME - " + offset);
			if (newShot == false) {
				currentYUV = fReader.read(offset);
				currentYMatrix = currentYUV.getY();
				// currentUMatrix = currentYUV.getU();
				// currentVMatrix = currentYUV.getV();
				yFrameAvg = getFrameAvg();
				numOfFrames++;
				interFrameDiffEstimateY = blockMatchingAlgo(currentYMatrix, prevYMatrix);
				// interFrameDiffEstimateU = blockMatchingAlgo(currentUMatrix,
				// prevUMatrix);
				// interFrameDiffEstimateV = blockMatchingAlgo(currentVMatrix,
				// prevVMatrix);

				if (firstFrameDiffEstimate == false) {
					if (Math.abs(interFrameDiffEstimateY - shotBasedDiffEstimateY) > (0.6 * shotBasedDiffEstimateY)) {
						// Exceeded threshold
						newShot = true;
						firstFrameDiffEstimate = true;
						shots.get(shots.size() - 1).setLengthOfShot(
								(offset * fReader.getLen()) - shots.get(shots.size() - 1).getStartingByte());
						shots.get(shots.size() - 1).setyMean(currentShotLumTotal / (numOfFrames - 1));
						System.out.println("LENGTH OF SHOT IN FRAMES - " + (shots.get(shots.size()-1).getLengthOfShot())/388800);
						currentShotLumTotal = 0;
						shotBasedDiffEstimateY = 0;
						// shotBasedDiffEstimateU = 0;
						// shotBasedDiffEstimateV = 0;
						numOfFrames = 0;
						// System.out.println(shots.get(shots.size()-1).getyMean());
					} else {
						currentShotLumTotal += yFrameAvg;
						offset += 2;
					}
				} else if (firstFrameDiffEstimate == true) {
					currentShotLumTotal += yFrameAvg;
					offset += 2;
					firstFrameDiffEstimate = false;
				}
				shotBasedDiffEstimateY = interFrameDiffEstimateY;
				// shotBasedDiffEstimateU += interFrameDiffEstimateU;
				// shotBasedDiffEstimateV += interFrameDiffEstimateV;
			}

			if (newShot == true) {
				shots.add(new Shot());
				shots.get(shots.size() - 1).setStartingByte(offset * fReader.getLen());
				currentYUV = fReader.read(offset);
				currentYMatrix = currentYUV.getY();
				// currentUMatrix = currentYUV.getU();
				// currentVMatrix = currentYUV.getV();
				yFrameAvg = getFrameAvg();
				numOfFrames++;
				currentShotLumTotal = yFrameAvg;
				offset += 2;
				newShot = false;
			}

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					prevYMatrix[x][y] = currentYMatrix[x][y];
					// prevUMatrix[x][y] = currentUMatrix[x][y];
					// prevVMatrix[x][y] = currentVMatrix[x][y];
				}
			}
		}

		if (numOfFrames != 0) {
			shots.get(shots.size() - 1)
					.setLengthOfShot(fReader.getFileLength() - shots.get(shots.size() - 1).getStartingByte());
			shots.get(shots.size() - 1).setyMean(currentShotLumTotal / (numOfFrames - 1));
		}
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

	private static void makeScenes() {

	}

	private static double getFrameAvg() {
		double frameAvg = 0;
		for (int y = 0; y < width; y++) {
			for (int x = 0; x < height; x++) {
				frameAvg += currentYMatrix[y][x];
			}
		}
		return frameAvg / (width * height);
	}

	private static double blockMatchingAlgo(double[][] currentFrame, double[][] prevFrame){
		double frameDiff = 0;
		double blockDiff = 0;
		double macroblockDiff = 0;
		int stepSize, posX, posY, startX, startY;
		
		for(int x = 0; x<(width-16); x++){
			for(int y = 0; y<(height-16); y++){
				macroblockDiff = 0;
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
							blockDiff = 0;
							for(int iDisp = 0; iDisp<16; iDisp++){
								for(int jDisp = 0; jDisp<16; jDisp++){
									//System.out.println((x+iDisp) + " " + (y+jDisp));
									blockDiff += (currentFrame[startX+iDisp][startY+jDisp] - prevFrame[startX+regionX+iDisp][startY+regionY+jDisp]);
								}
							}
							macroblockDiff = Math.min(macroblockDiff, blockDiff/256);
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
		if (args.length != 1) {
			Utilities.die("Not enough arguments! \nUsage java MyProgram input.rgb input.wav output.rgb output.wav");
		}

		inputVideoFile = args[0];
		// inputAudioFile = args[1];
		// outputVideoFile = args[2];
		// outputAudioFile = args[3];
	}

}