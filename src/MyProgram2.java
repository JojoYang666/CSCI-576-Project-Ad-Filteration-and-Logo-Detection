import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MyProgram2 {
	final public static String STARBUCKS_LOGO = "../../dataset/Brand Images/starbucks_logo.rgb";
	final public static String STARBUCKS_RGB = "../../dataset/Ads/Starbucks_Ad_15s.rgb";
	final public static String STARBUCKS_WAV = "../../dataset/Ads/Starbucks_Ad_15s.wav";
	final public static String SUBWAY_LOGO = "../../dataset/Brand Images/subway_logo.rgb";
	final public static String SUBWAY_RGB = "../../dataset/Ads/Subway_Ad_15s.rgb";
	final public static String SUBWAY_WAV = "../../dataset/Ads/Subway_Ad_15s.wav";
	final public static String MCD_LOGO = "../../dataset2/Brand Images/Mcdonalds_logo.raw";
	final public static String MCD_RGB = "../../dataset2/Ads/mcd_Ad_15s.rgb";
	final public static String MCD_WAV = "../../dataset2/Ads/mcd_Ad_15s.wav";
	final public static String NFL_LOGO = "../../dataset2/Brand Images/nfl_logo.rgb";
	final public static String NFL_RGB = "../../dataset2/Ads/nfl_Ad_15s.rgb";
	final public static String NFL_WAV = "../../dataset2/Ads/nfl_Ad_15s.wav";

	private static ArrayList<Shot> shots;
	private static final int width = 480, height = 270;
	private static final int BYTES_PER_VIDEO_FRAME = 388800;
	private ArrayList<Scene> scenes;
	private static ArrayList<Double> valsY = new ArrayList<Double>();
	private static ArrayList<Double> valsU = new ArrayList<Double>();
	private static ArrayList<Double> valsV = new ArrayList<Double>();
	private static String inputVideoFile, inputAudioFile, outputVideoFile, outputAudioFile;
	private static int sizeOfSlidingWindow = 5;
	private static boolean searchLogoFlag;
	private static double[][] currentYMatrix = new double[width][height];
	private static double[][] prevYMatrix = new double[width][height];
	private static double[][] currentUMatrix = new double[width][height];
	private static double[][] prevUMatrix = new double[width][height];
	private static double[][] currentVMatrix = new double[width][height];
	private static double[][] prevVMatrix = new double[width][height];
	private static YUV currentYUV;

	public static void main(String[] args) {
		parseArgs(args);
		FindStarbucks.preprocess(inputVideoFile, STARBUCKS_LOGO);
		FindSubway.preprocess(inputVideoFile, SUBWAY_LOGO);
		FindMcDonalds.preprocess(inputVideoFile, MCD_LOGO);
		FindNfl.preprocess(inputVideoFile, NFL_LOGO);
		FrameReader fReader = new FrameReader(inputVideoFile, width, height);
		makeShots(fReader);
		AudioCut.voteShots(shots, inputAudioFile);
		if (searchLogoFlag) {
			searchFramesForLogo();
		}
		// System.out.println("SIZE OF FILE - " + fReader.getFileLength());
		// System.out.println("SIZE OF SHOTS - " + shots.size());
		/*
		 * for (Shot s : shots) { System.out.println("SHOT: " +
		 * s.getStartingByte() + "   " + s.getLengthOfShot() + " | " + s.isAd()
		 * + " " + s.getAudioVoteCount() + " " + s.getStartingFrame() + " " +
		 * s.getEndingFrame()); }
		 */
		// writeToDisk();
		writeScenesToDisk();
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

		// System.out.println("MAX NUMBER OF FRAMES - " + maxNumOfFrames);

		// TODO Audio processing
		while (offset < maxNumOfFrames) {
			// System.out.println("CURRENT FRAME - " + offset );
			if (startProcess == false) {
				currentYUV = fReader.read();
				currentYMatrix = currentYUV.getY();
				currentUMatrix = currentYUV.getU();
				currentVMatrix = currentYUV.getV();
				interFrameDiffEstimateY = blockMatchingAlgo(currentYMatrix, prevYMatrix);
				interFrameDiffEstimateU = blockMatchingAlgo(currentUMatrix, prevUMatrix);
				interFrameDiffEstimateV = blockMatchingAlgo(currentVMatrix, prevVMatrix);

				if (valsY.size() == sizeOfSlidingWindow)
					valsY.remove(0);
				if (valsU.size() == sizeOfSlidingWindow)
					valsU.remove(0);
				if (valsV.size() == sizeOfSlidingWindow)
					valsV.remove(0);

				if (firstFrameDiffEstimate == true) {
					firstFrameDiffEstimate = false;
					valsY.add((double) interFrameDiffEstimateY);
					valsU.add((double) interFrameDiffEstimateU);
					valsV.add((double) interFrameDiffEstimateV);
				} else {
					votes = 0;
					if (interFrameDiffEstimateY > yThreshold)
						votes++;
					if (interFrameDiffEstimateU > uThreshold)
						votes++;
					if (interFrameDiffEstimateV > vThreshold)
						votes++;
					if (votes >= 2) {
						// System.out.println("THAT'S A SHOT, BOYS! " + offset);
						shots.get(shots.size() - 1).setLengthOfShot(
								(offset * fReader.getLen()) - shots.get(shots.size() - 1).getStartingByte());
						shots.get(shots.size() - 1)
								.setEndingFrame(((offset * fReader.getLen()) / BYTES_PER_VIDEO_FRAME) - 1);
						// System.out.println(shots.get(shots.size()-1).getStartingByte()
						// + " " + offset*fReader.getLen() + " " +
						// shots.get(shots.size()-1).getLengthOfShot());
						shots.add(new Shot());
						shots.get(shots.size() - 1).setStartingByte(offset * fReader.getLen());
						shots.get(shots.size() - 1)
								.setStartingFrame((offset * fReader.getLen()) / BYTES_PER_VIDEO_FRAME);
						firstFrameDiffEstimate = true;
						valsY.clear();
						valsU.clear();
						valsV.clear();
					} else {
						valsY.add((double) interFrameDiffEstimateY);
						valsU.add((double) interFrameDiffEstimateU);
						valsV.add((double) interFrameDiffEstimateV);
					}
				}

				yThreshold = 2 * calcThreshold(valsY);
				uThreshold = 2 * calcThreshold(valsU);
				vThreshold = 2 * calcThreshold(valsV);
				offset += 1;
			}

			if (startProcess == true) {
				// System.out.println("Starting with " + offset);
				shots.add(new Shot());
				shots.get(shots.size() - 1).setStartingByte(offset * fReader.getLen());
				shots.get(shots.size() - 1).setStartingFrame((offset * fReader.getLen()) / BYTES_PER_VIDEO_FRAME);
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

		shots.get(shots.size() - 1)
				.setLengthOfShot(fReader.getFileLength() - shots.get(shots.size() - 1).getStartingByte());
		shots.get(shots.size() - 1).setEndingFrame((fReader.getFileLength() / BYTES_PER_VIDEO_FRAME) - 1);
	}

	private static double calcThreshold(ArrayList<Double> vals) {
		double avg = 0, sumOfDiff = 0;
		for (int i = 0; i < vals.size(); i++) {
			avg += vals.get(i);
		}
		avg /= vals.size();

		for (int i = 0; i < vals.size(); i++) {
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

	private static void writeScenesToDisk() {
		final int MAX_LIMIT = 999999999;
		InputStream audioInputStream = null;
		InputStream videoInputStream = null;

		try {
			File videoFile = new File(inputVideoFile);
			videoInputStream = new FileInputStream(videoFile);
		} catch (FileNotFoundException e) {
			Utilities.die("File Not Found: " + inputVideoFile);
		}
		try {
			File audioFile = new File(inputAudioFile);
			audioInputStream = new FileInputStream(audioFile);
		} catch (FileNotFoundException e) {
			Utilities.die("File Not Found: " + inputAudioFile);
		}

		try {
			FileOutputStream videoWriter = new FileOutputStream(new File(outputVideoFile));
			FileOutputStream audioWriter = new FileOutputStream(new File(outputAudioFile));

			byte[] bytes;
			byte[] audioBytes = new byte[44];
			audioInputStream.read(audioBytes);
			audioWriter.write(audioBytes);
			boolean previousIsAd = false;
			int logo = -1;
			int[] logoCount = new int[] { 0, 0, 0, 0 };
            float[] confidenceCount = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
			long counter = 0; // could be used to rewrite wav header
			for (Shot s : shots) {
				if (s.isAd()) {
					videoInputStream.skip(s.getLengthOfShot());
					audioInputStream.skip(s.getNumberOfFrames() * AudioCut.SAMPLES_PER_FRAME * 2);
					previousIsAd = true;

					float max = -1;
					logo = -1;
					for (int i = 0; i < logoCount.length; i++) {
                        if (logoCount[i] > 0) {
                            float average = confidenceCount[i] / logoCount[i];
                            if (average > max) {
                                max = average;
                                logo = i;
                            }
						}
					}
					
					//System.out.println("logo: " + logo + " with max value of " + max);
					counter += (s.getNumberOfFrames() * AudioCut.SAMPLES_PER_FRAME * 2);

				} else {
					if (searchLogoFlag) {

						if (previousIsAd) {
							InputStream logoVideoInputStream = null;
							InputStream logoAudioInputStream = null;
							if (logo == 0) {
								File logoVideoFile = new File(STARBUCKS_RGB);
								logoVideoInputStream = new FileInputStream(logoVideoFile);
								File logoAudioFile = new File(STARBUCKS_WAV);
								logoAudioInputStream = new FileInputStream(logoAudioFile);
							} else if (logo == 1) {
								File logoVideoFile = new File(SUBWAY_RGB);
								logoVideoInputStream = new FileInputStream(logoVideoFile);
								File logoAudioFile = new File(SUBWAY_WAV);
								logoAudioInputStream = new FileInputStream(logoAudioFile);
							} else if (logo == 2) {
								File logoVideoFile = new File(MCD_RGB);
								logoVideoInputStream = new FileInputStream(logoVideoFile);
								File logoAudioFile = new File(MCD_WAV);
								logoAudioInputStream = new FileInputStream(logoAudioFile);
							} else if (logo == 3) {
								File logoVideoFile = new File(NFL_RGB);
								logoVideoInputStream = new FileInputStream(logoVideoFile);
								File logoAudioFile = new File(NFL_WAV);
								logoAudioInputStream = new FileInputStream(logoAudioFile);
							}
							if (logoVideoInputStream != null) {
								while (logoVideoInputStream.available() != 0) {
									bytes = new byte[logoVideoInputStream.available()];
									logoVideoInputStream.read(bytes);
									videoWriter.write(bytes);
								}
							}
							if (logoAudioInputStream != null) {
								logoAudioInputStream.skip(44);
								while (logoAudioInputStream.available() != 0) {
									audioBytes = new byte[logoAudioInputStream.available()];
									logoAudioInputStream.read(audioBytes);
									audioWriter.write(audioBytes);
								}
							}
							previousIsAd = false;
							logoCount = new int[] { 0, 0, 0, 0 };
						}

						ArrayList<VideoFrame> frames = s.getFramesWithLogo();
						long framesRemaining = s.getNumberOfFrames();
						//System.out.println("Initial number of remainining frames - " + framesRemaining);
						int currentFrameIndex = (int) s.getStartingFrame();
						//System.out.println("Starting frame index - " + currentFrameIndex);
						int vfIndex = 0;
						VideoFrame vf = null;
						if (frames.size() > 0) {
							vf = frames.get(vfIndex);
						}

						while (framesRemaining > 0) {
							byte[] aFrame = new byte[3 * width * height];
							videoInputStream.read(aFrame);
							if (vf != null) {
								if (vf.getFrameNumber() == currentFrameIndex) {
									//System.out.print("boxing " + vf.getFrameNumber() + ": " + vf.getLogo() + " ");
									boxLogo(aFrame, vf.getLogo(), vf.getLogoLocation());

									logoCount[vf.getLogo()]++;
                                    confidenceCount[vf.getLogo()] += vf.getConfidence();

									vfIndex++;
									if (frames.size() > vfIndex) {
										vf = frames.get(vfIndex);
									} else {
										vf = null;
									}
								}
							}
							videoWriter.write(aFrame);
							currentFrameIndex++;
//							framesRemaining -= aFrame.length;
							framesRemaining -= 1;
							//System.out.println("Frames remaining right now - " + framesRemaining);
						}
						/*
						 * if (frames.size() > 0) {
						 * 
						 * long index = vf.getFrameNumber();
						 * 
						 * long diff = index - currentFrameIndex; if (diff != 0)
						 * { long bytesRemaining = diff * (height * width * 3);
						 * while (bytesRemaining != 0) { if (bytesRemaining >=
						 * MAX_LIMIT) { bytes = new byte[MAX_LIMIT]; } else {
						 * bytes = new byte[(int)bytesRemaining]; }
						 * videoInputStream.read(bytes);
						 * videoWriter.write(bytes); bytesRemaining -=
						 * bytes.length; } currentFrameIndex += diff;
						 * framesRemaining -= diff; }
						 * 
						 * boolean doneProcessingFramesWithLogo = false; if
						 * (frames.size() == 0) { doneProcessingFramesWithLogo =
						 * true; }
						 * 
						 * while (!doneProcessingFramesWithLogo) { byte[] aFrame
						 * = new byte[3 * width * height];
						 * videoInputStream.read(aFrame); if (currentFrameIndex
						 * == index) { System.out.print(index + ": ");
						 * boxLogo(aFrame, vf.getLogo(), vf.getLogoLocation());
						 * index++; } videoWriter.write(aFrame);
						 * 
						 * currentFrameIndex++; framesRemaining--; if (index >=
						 * frames.size()) { doneProcessingFramesWithLogo = true;
						 * } else { vf = frames.get((int)index); } } } if
						 * (framesRemaining != 0) { long bytesRemaining =
						 * framesRemaining * (height * width * 3); while
						 * (bytesRemaining != 0) { if (bytesRemaining >=
						 * MAX_LIMIT) { bytes = new byte[MAX_LIMIT]; } else {
						 * bytes = new byte[(int)bytesRemaining]; }
						 * videoInputStream.read(bytes);
						 * videoWriter.write(bytes); bytesRemaining -=
						 * bytes.length; } }
						 */
					} else {
						long bytesRemaining = s.getLengthOfShot();
						while (bytesRemaining != 0) {
							if (bytesRemaining >= MAX_LIMIT) {
								bytes = new byte[MAX_LIMIT];
							} else {
								bytes = new byte[(int) bytesRemaining];
							}
							videoInputStream.read(bytes);
							videoWriter.write(bytes);
							bytesRemaining -= bytes.length;
						}
					}
					long audioBytesRemaining = s.getNumberOfFrames() * AudioCut.SAMPLES_PER_FRAME * 2;
					while (audioBytesRemaining != 0) {
						if (audioBytesRemaining >= MAX_LIMIT) {
							audioBytes = new byte[MAX_LIMIT];
						} else {
							audioBytes = new byte[(int) audioBytesRemaining];
						}
						audioInputStream.read(audioBytes);
						audioWriter.write(audioBytes);
						audioBytesRemaining -= audioBytes.length;
						counter += audioBytes.length;
					}
				}
			}
			videoWriter.close();
			// rewrite wav header data size here
			//System.out.println(counter);
			audioWriter.close();
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
		}
	}

	private static void boxLogo(byte[] data, int logo, int location) {
		//System.out.println(location);
        int r = 0;
        int g = 255;
        int b = 0;
        if (logo == 1) {
            r = 255;
            g = 255;
            b = 0;
        }
        else if (logo == 2) {
            r = 255;
            g = 0;
            b = 0;
        }
        else if (logo == 3) {
            r = 0;
            g = 0;
            b = 255;
        }
      
		for (int i = location; i < (location + 90); i++) {
			data[i] = (byte)r;
            data[i + (width * height)] = (byte)g;
            data[i + (width * height * 2)] = (byte)b;
          
            data[i + (90 * width)] = (byte)r;
            data[i + (width * height) + (90 * width)] = (byte)g;
            data[i + (width * height * 2) + (90 * width)] = (byte)b;
		}
        for (int i = 0; i < 90; i++) {
            data[(i * width) + location] = (byte)r;
            data[(i * width) + location + (width * height)] = (byte)g;
            data[(i * width) + location + (width * height * 2)] = (byte)b;
            
            data[(i * width) + location + 90] = (byte)r;
            data[(i * width) + location + (width * height) + 90] = (byte)g;
            data[(i * width) + location + (width * height * 2) + 90] = (byte)b;
        }
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

	private static double blockMatchingAlgo(double[][] currentFrame, double[][] prevFrame) {
		double frameDiff = 0;
		double blockDiff = 0;
		double macroblockDiff = 0;
		int stepSize, posX, posY, startX, startY;

		for (int x = 0; x < (width - 16); x += 16) {
			for (int y = 0; y < (height - 16); y += 16) {
				macroblockDiff = Double.MAX_VALUE;
				stepSize = 4;
				startX = x;
				startY = y;
				posX = x;
				posY = y;

				while (stepSize > 0) {
					startX = posX;
					startY = posY;
					for (int regionX = -stepSize; regionX <= stepSize; regionX += stepSize) {
						if ((startX + regionX < 0) | (startX + regionX >= width) | (startX + regionX + 16 >= width))
							continue;
						for (int regionY = -stepSize; regionY <= stepSize; regionY += stepSize) {
							if ((startY + regionY < 0) | (startY + regionY >= height)
									| (startY + regionY + 16 >= height))
								continue;
							if (regionX == 0 && regionY == 0)
								continue;
							blockDiff = 0;
							for (int iDisp = 0; iDisp < 16; iDisp++) {
								for (int jDisp = 0; jDisp < 16; jDisp++) {
									blockDiff += Math.abs(currentFrame[startX + iDisp][startY + jDisp]
											- prevFrame[startX + regionX + iDisp][startY + regionY + jDisp]);
								}
							}
							if (macroblockDiff > blockDiff / 256) {
								posX = startX + regionX;
								posY = startY + regionY;
								macroblockDiff = blockDiff / 256;
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
		if (args.length < 4) {
			Utilities
					.die("Not enough arguments! \nUsage java MyProgram input.rgb input.wav output.rgb output.wav [-l]");
		}
		if (args.length > 5) {
			Utilities.die("Too many arguments! \nUsage java MyProgram input.rgb input.wav output.rgb output.wav [-l]");
		}

		inputVideoFile = args[0];
		inputAudioFile = args[1];
		outputVideoFile = args[2];
		outputAudioFile = args[3];
		searchLogoFlag = false;

		if (args.length == 5) {
			searchLogoFlag = true;
		}

		if (!outputVideoFile.endsWith(".rgb")) {
			outputVideoFile += ".rgb";
		}
		if (!outputAudioFile.endsWith(".wav")) {
			outputAudioFile += ".wav";
		}
	}

	private static void searchFramesForLogo() {
        System.out.println("Started logo searching");
		for (Shot s : shots) {
			if (!s.isAd()) {
				ArrayList<VideoFrame> frames = s.getFramesWithLogo();
				for (long l = s.getStartingFrame(); l < s.getEndingFrame(); l += 2) {
					VideoFrame vf = new VideoFrame();
					vf.setFrameNumber((int) l);
					FindStarbucks.findLogo(vf);
					//System.out.print(l + ": Finding Starbucks ");
					FindSubway.findLogo(vf);
					//System.out.print("Subway ");
					FindMcDonalds.findLogo(vf);
					//System.out.print("McDonalds ");
					FindNfl.findLogo(vf);
					//System.out.print("NFL ");
					if (vf.getLogo() != -1) {
						frames.add(vf);
						//System.out.print("added " + vf.getLogo() + " ");
					}
					//System.out.println("done");
				}
			}
/*
            else {
				System.out.println("Ads from " + s.getStartingFrame() + " to " + s.getEndingFrame());
			}
*/
		}
        System.out.println("Done logo searching");
	}
}