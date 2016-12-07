import java.io.IOException;
import java.io.RandomAccessFile;

public class FindSubway {
	private static int BYTES_PER_FRAME = 388800;
	// private static int FRAME_NO;
	private static int GRID_SIZE = 90;
	private static int BANDS = 16;
	private static int TOTAL_PIXELS_LOGO;
	private static int TOTAL_COLORS_LOGO;
	// private static float LOGO_ENTROPY;
	private static int WIDTH = 480, HEIGHT = 270;
	private static String videoFile, logoFile;
	private static RandomAccessFile videoRand;
	private static RandomAccessFile logoRand;
	private static int[][] vidH = new int[WIDTH][HEIGHT];
	private static int[][] vidS = new int[WIDTH][HEIGHT];
	private static int[][] vidV = new int[WIDTH][HEIGHT];
	private static int[][] vidFilteredH = new int[WIDTH][HEIGHT];
	private static int[][] logoH = new int[WIDTH][HEIGHT];
	private static float[] logoProportionsHHist = new float[BANDS];
	private static int[][] logoS = new int[WIDTH][HEIGHT];
	private static int[][] logoV = new int[WIDTH][HEIGHT];
	private static int[] vidHHist = new int[BANDS];
	private static float[] vidProportionsHHist = new float[BANDS];
	private static int[] logoHHist = new int[BANDS];
	private static int[] vidWBHist = new int[2];
	private static float[] vidWBProportionsHist = new float[2];
	private static int[] logoWBHist = new int[2];
	private static float[] logoProportionsWBHist = new float[2];
	private static float[] quotientHHist = new float[BANDS];
	private static float[] quotientWBHist = new float[2];
	private static int tx = -1, ty = -1;
	private static float logoConfidence = -Float.MAX_VALUE;
	private static float paramountConfidence;

	public static void preprocess(String vidFileName, String logoFileName) {
		videoFile = vidFileName;
		logoFile = logoFileName;
		openLogo(logoFile);
		readFile(0, logoRand, logoH, logoS, logoV);
		makeHistogramForLogo(logoH, logoS, logoV, logoHHist, logoWBHist);
		keepTopValsOfHistogram(logoHHist, logoWBHist);
		TOTAL_PIXELS_LOGO = calcTotalPixels(logoHHist, logoWBHist);
		TOTAL_COLORS_LOGO = calcTotalColors(logoHHist, logoWBHist);
		proportionalizeHistogram(logoHHist, logoWBHist, logoProportionsHHist, logoProportionsWBHist, TOTAL_PIXELS_LOGO);
		try {
			logoRand.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void findLogo(VideoFrame frameObj) {

		// videoFile = args[0];
		// logoFile = args[1];

		logoConfidence = 0;
		paramountConfidence = 0;
		tx = -1;
		ty = -1;
		openVideo(videoFile);
		// openLogo(logoFile);
		readFile(frameObj.getFrameNumber(), videoRand, vidH, vidS, vidV);
		try {
			videoRand.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// readFile(0, logoRand, logoH, logoS, logoV);
		filterFrame(vidH, vidFilteredH);

		// makeHistogramForLogo(logoH, logoS, logoV, logoHHist, logoWBHist);
		// keepTopValsOfHistogram(logoHHist, logoWBHist);
		// //System.out.println("HISTOGRAM OF LOGO AFTER:");
		// for (int i = 0; i < BANDS; i++) {
		// //System.out.print("(" + i + ": " + logoHHist[i] + ")\t");
		// }
		// //System.out.println("WHITE PIXELS - " + logoWBHist[0]);
		// //System.out.println("BLACK PIXELS - " + logoWBHist[1] + "\n\n");

		// TOTAL_PIXELS_LOGO = calcTotalPixels(logoHHist, logoWBHist);
		// TOTAL_COLORS_LOGO = calcTotalColors(logoHHist, logoWBHist);
		// LOGO_ENTROPY = calcEntropy(logoHHist, logoWBHist, TOTAL_PIXELS_LOGO);
		// proportionalizeHistogram(logoHHist, logoWBHist, logoProportionsHHist,
		// logoProportionsWBHist, TOTAL_PIXELS_LOGO);
		// //System.out.println("LOGO ENTROPY - " + LOGO_ENTROPY);
		// //System.out.println("TOTAL PIXELS: " + TOTAL_PIXELS_LOGO);
		// //System.out.println("TOTAL COLORS: " + TOTAL_COLORS_LOGO);

		// //System.out.println("HISTOGRAM OF LOGO AFTER:");
		// for (int i = 0; i < BANDS; i++) {
		// System.out.print("(" + i + ": " + logoProportionsHHist[i] +
		// ")\t");
		// }
		// //System.out.println("WHITE PIXELS - " + logoProportionsWBHist[0]);
		// //System.out.println("BLACK PIXELS - " + logoProportionsWBHist[1]);

		float temp;
		for (int y = 45; y < (HEIGHT - 45 - GRID_SIZE); y += 10) {
			for (int x = 45; x < (WIDTH - 45 - GRID_SIZE); x += 10) {
				// System.out.println("\n\n##############################################");
				// System.out.println(x + " " + y);

				makeHistogramForFrame(vidFilteredH, vidS, vidV, vidHHist, vidWBHist, x, y);
				// keepTopValsOfHistogram(vidHHist, vidWBHist);
				// System.out.println(((int) Math.ceil(x / GRID_SIZE)) + "\t" +
				// ((int) Math.ceil(y / GRID_SIZE)));
				temp = getQuotientHist(vidHHist, vidWBHist, quotientHHist, quotientWBHist, vidProportionsHHist,
						vidWBProportionsHist);
				if (temp > logoConfidence) {
					logoConfidence = temp;
					tx = x;
					ty = y;
				}
			}
		}

		if(logoConfidence>0.85){
			if(frameObj.getConfidence()<logoConfidence){
				frameObj.setConfidence(logoConfidence);
				frameObj.setLogo(1);
				frameObj.setLogoLocation(ty*WIDTH + tx);
			}
		}
	}

	private static void keepTopValsOfHistogram(int[] hHist, int[] wbHist) {
		int[] topVals = new int[BANDS + 2];
		int temp, flag = 0;

		for (int i = 0; i < BANDS; i++)
			topVals[i] = hHist[i];

		topVals[BANDS] = wbHist[0];
		topVals[BANDS + 1] = wbHist[1];

		for (int i = 0; i < (BANDS + 2); i++) {
			for (int j = i + 1; j < (BANDS + 2); j++) {
				if (topVals[i] < topVals[j]) {
					temp = topVals[i];
					topVals[i] = topVals[j];
					topVals[j] = temp;
				}
			}
		}

		for (int i = 0; i < BANDS; i++) {
			flag = 0;
			for (int j = 0; j < 4; j++) {
				if (hHist[i] == topVals[j]) {
					flag = 1;
					break;
				}
			}
			if (flag == 0)
				hHist[i] = 0;
		}

		for (int i = 0; i < 2; i++) {
			flag = 0;
			for (int j = 0; j < 4; j++) {
				if (wbHist[i] == topVals[j]) {
					flag = 1;
					break;
				}
			}
			if (flag == 0)
				wbHist[i] = 0;
		}

	}

	private static void proportionalizeHistogram(int[] hHist, int[] wbHist, float[] proportionsH, float[] proportionsWb,
			int totalPixels) {
		for (int i = 0; i < BANDS; i++) {
			proportionsH[i] = (float) hHist[i] / totalPixels;
		}
		proportionsWb[0] = (float) wbHist[0] / totalPixels;
		proportionsWb[1] = (float) wbHist[1] / totalPixels;
	}

	private static float piShapeFuncValue(float a, float b, float c, float d, float x) {
		if (x <= a)
			return 0;
		else if (a <= x && x <= ((a + b) / 2))
			return (float) (2 * Math.pow((float) ((x - a) / (b - a)), 2));
		else if (((a + b) / 2) <= x && x <= b)
			return (float) (1 - Math.pow((float) (x - b) / (b - a), 2));
		else if (b <= x && x <= c)
			return 1;
		else if (c <= x && x <= ((c + d) / 2))
			return (float) (1 - Math.pow((float) (x - c) / (d - c), 2));
		else if (((c + d) / 2) <= x && x <= d)
			return (float) (2 * Math.pow((float) ((x - d) / (d - c)), 2));
		else if (x >= d)
			return 0;
		return 0;
	}

	private static void filterFrame(int[][] H, int[][] filteredH) {
		int temp, ind;
		int[] medianFilter = new int[9];

		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				ind = 0;
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						if ((x + i) < 0 || (x + i) >= WIDTH)
							continue;
						if ((y + j) < 0 || (y + j) >= HEIGHT)
							continue;

						medianFilter[ind++] = H[x + i][y + j];
					}
				}

				for (int i = 0; i < ind; i++) {
					for (int j = i + 1; j < ind; j++) {
						if (medianFilter[i] > medianFilter[j]) {
							temp = medianFilter[i];
							medianFilter[i] = medianFilter[j];
							medianFilter[j] = temp;
						}
					}
				}
				filteredH[x][y] = medianFilter[ind / 2];
			}
		}
	}

	private static int calcTotalColors(int[] hHist, int[] wbHist) {
		int ans = 0;

		for (int i = 0; i < BANDS; i++) {
			if (hHist[i] != 0)
				ans += 1;
		}

		if (wbHist[0] != 0)
			ans += 1;
		if (wbHist[1] != 0)
			ans += 1;

		return ans;
	}

	private static int calcTotalPixels(int[] hHist, int[] wbHist) {
		int ans = 0;

		for (int i = 0; i < BANDS; i++)
			ans += hHist[i];
		ans += wbHist[0] + wbHist[1];

		return ans;
	}

	private static float calcEntropy(int[] hHist, int[] wbHist, int totalCount) {
		float ans = 0, prob;

		for (int i = 0; i < BANDS; i++) {
			prob = (float) hHist[i] / totalCount;
			if (prob != 0.0)
				ans += (prob * (Math.log(prob) / Math.log(2)));
		}
		prob = (float) wbHist[0] / totalCount;
		if (prob != 0.0)
			ans += (prob * (Math.log(prob) / Math.log(2)));
		prob = (float) wbHist[1] / totalCount;
		if (prob != 0.0)
			ans += (prob * (Math.log(prob) / Math.log(2)));
		return (ans * -1);
	}

	public static void openVideo(String fileName) {
		try {
			videoRand = new RandomAccessFile(fileName, "r");
		} catch (IOException e) {
			Utilities.die("File not found - " + fileName);
		}
	}

	public static void openLogo(String fileName) {
		try {
			logoRand = new RandomAccessFile(fileName, "r");
		} catch (IOException e) {
			Utilities.die("File not found - " + fileName);
		}
	}

	public static void readFile(int offset, RandomAccessFile randFile, int[][] H, int[][] S, int[][] V) {
		byte[] bytes = new byte[BYTES_PER_FRAME];
		// float[] hsbVals = new float[3];
		double[] hslVals = new double[3];
		try {
			randFile.skipBytes((int) (offset * BYTES_PER_FRAME));
			randFile.read(bytes, 0, BYTES_PER_FRAME);

			int ind = 0;

			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {

					byte r = bytes[ind];
					byte g = bytes[ind + HEIGHT * WIDTH];
					byte b = bytes[ind + HEIGHT * WIDTH * 2];
					ind++;

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					int Bl = (pix) & 0xff;
					int G = (pix >> 8) & 0xff;
					int R = (pix >> 16) & 0xff;

					hslVals = colorConvert.ColorSpaceConverter.RGBtoHSL(R, G, Bl);
					H[x][y] = Math.round((float) hslVals[0]);
					S[x][y] = Math.round((float) hslVals[1] * 100);
					V[x][y] = Math.round((float) hslVals[2] * 100);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void makeHistogramForLogo(int[][] H, int[][] S, int[][] V, int[] hHist, int[] wbHist) {
		boolean beg = false, end = false;
		int begPosX = 0, endPosX = 0;
		int mostCommonH, mostCommonS, mostCommonV;

		mostCommonH = H[2][2];
		mostCommonS = S[2][2];
		mostCommonV = V[2][2];

		for (int i = 0; i < BANDS; i++)
			hHist[i] = 0;
		wbHist[0] = 0;
		wbHist[1] = 0;

		for (int y = 1; y < (HEIGHT - 1); y++) {
			beg = false;
			end = false;
			for (int x = 1; x < WIDTH; x++) {
				if ((H[x][y] != mostCommonH || S[x][y] != mostCommonS || V[x][y] != mostCommonV) && beg != true) {
					beg = true;
					begPosX = x;
				}

				if ((H[WIDTH - x][y] != mostCommonH || S[WIDTH - x][y] != mostCommonS || V[WIDTH - x][y] != mostCommonV)
						&& end != true) {
					end = true;
					endPosX = WIDTH - x;
				}

				if (beg == true && end == true) {
					for (int i = begPosX; i <= endPosX; i++) {
						if (V[i][y] >= 87.5 && V[i][y] <= 100)
							wbHist[0]++;
						else if (V[i][y] >= 0 && V[i][y] <= 12.5)
							wbHist[1]++;
						else {
							if (V[i][y] > 12.5 && V[i][y] < 87.5 && S[i][y] >= 70) {
								if (H[i][y] >= 357 || H[i][y] <= 8) {
									// Red
									hHist[0]++;
								} else if (H[i][y] >= 9 && H[i][y] <= 23) {
									// Red-orange
									hHist[1]++;
								} else if (H[i][y] >= 24 && H[i][y] <= 39) {
									// Orange-brown
									hHist[2]++;
								} else if (H[i][y] >= 40 && H[i][y] <= 51) {
									// Orange-yellow
									hHist[3]++;
								} else if (H[i][y] >= 52 && H[i][y] <= 59) {
									// Yellow
									hHist[4]++;
								} else if (H[i][y] >= 60 && H[i][y] <= 79) {
									// Yellow-green
									hHist[5]++;
								} else if (H[i][y] >= 80 && H[i][y] <= 134) {
									// Green
									hHist[6]++;
								} else if (H[i][y] >= 135 && H[i][y] <= 179) {
									// Green-cyan
									hHist[7]++;
								} else if (H[i][y] >= 180 && H[i][y] <= 202) {
									// Cyan
									hHist[8]++;
								} else if (H[i][y] >= 203 && H[i][y] <= 219) {
									// Cyan-blue
									hHist[9]++;
								} else if (H[i][y] >= 220 && H[i][y] <= 254) {
									// Blue
									hHist[10]++;
								} else if (H[i][y] >= 255 && H[i][y] <= 278) {
									// Blue-magenta
									hHist[11]++;
								} else if (H[i][y] >= 279 && H[i][y] <= 317) {
									// Magenta
									hHist[12]++;
								} else if (H[i][y] >= 318 && H[i][y] <= 336) {
									// Magenta-pink
									hHist[13]++;
								} else if (H[i][y] >= 337 && H[i][y] <= 347) {
									// Pink
									hHist[14]++;
								} else if (H[i][y] >= 348 && H[i][y] <= 356) {
									hHist[15]++;
								}
							}
						}
					}
					break;
				}
			}
		}
	}

	private static void makeHistogramForFrame(int[][] H, int[][] S, int[][] V, int[] hHist, int[] wbHist, int wx,
			int hy) {
		for (int i = 0; i < BANDS; i++)
			hHist[i] = 0;
		wbHist[0] = 0;
		wbHist[1] = 0;

		int limy = hy + GRID_SIZE, limx = wx + GRID_SIZE;
		if (limy >= HEIGHT)
			limy = HEIGHT;
		if (limx >= WIDTH)
			limx = WIDTH;
		// System.out.println("LIMX - " + limx + "\t" + "LIMY" + limy);

		for (int y = hy; y < limy; y++) {
			for (int x = wx; x < limx; x++) {
				if (V[x][y] >= 87.5 && V[x][y] <= 100)
					wbHist[0]++;
				else if (V[x][y] >= 0 && V[x][y] <= 12.5)
					wbHist[1]++;
				else {
					if (V[x][y] > 12.5 && V[x][y] < 87.5 && S[x][y] >= 20) {
						if (H[x][y] >= 357 || H[x][y] <= 8) {
							// Red
							hHist[0]++;
						} else if (H[x][y] >= 9 && H[x][y] <= 23) {
							// Red-orange
							hHist[1]++;
						} else if (H[x][y] >= 24 && H[x][y] <= 39) {
							// Orange-brown
							hHist[2]++;
						} else if (H[x][y] >= 40 && H[x][y] <= 51) {
							// Orange-yellow
							hHist[3]++;
						} else if (H[x][y] >= 52 && H[x][y] <= 59) {
							// Yellow
							hHist[4]++;
						} else if (H[x][y] >= 60 && H[x][y] <= 79) {
							// Yellow-green
							hHist[5]++;
						} else if (H[x][y] >= 80 && H[x][y] <= 134) {
							// Green
							hHist[6]++;
						} else if (H[x][y] >= 135 && H[x][y] <= 179) {
							// Green-cyan
							hHist[7]++;
						} else if (H[x][y] >= 180 && H[x][y] <= 202) {
							// Cyan
							hHist[8]++;
						} else if (H[x][y] >= 203 && H[x][y] <= 219) {
							// Cyan-blue
							hHist[9]++;
						} else if (H[x][y] >= 220 && H[x][y] <= 254) {
							// Blue
							hHist[10]++;
						} else if (H[x][y] >= 255 && H[x][y] <= 278) {
							// Blue-magenta
							hHist[11]++;
						} else if (H[x][y] >= 279 && H[x][y] <= 317) {
							// Magenta
							hHist[12]++;
						} else if (H[x][y] >= 318 && H[x][y] <= 336) {
							// Magenta-pink
							hHist[13]++;
						} else if (H[x][y] >= 337 && H[x][y] <= 347) {
							// Pink
							hHist[14]++;
						} else if (H[x][y] >= 348 && H[x][y] <= 356) {
							hHist[15]++;
						}
					}
				}
			}
		}
	}

	private static float getQuotientHist(int[] hHist, int[] wbHist, float[] quoHHist, float[] quoWbHist,
			float[] proportionsH, float[] proportionsWb) {
		int newPixelCount, colorCount = 0, totalCountGrid, gridBasedLogoPixels = 0;
		float entropyGrid = 0, scaleFactor, entropyLogo, prob, entropyMatch;
		float[] proportionHMatches = new float[BANDS], proportionWbMatches = new float[2];

		// System.out.println("\nHISTOGRAM OF GRID BEFORE:");
		for (int i = 0; i < BANDS; i++) {
			// System.out.print("(" + i + ": " + hHist[i] + ")\t");
		}
		// System.out.println("WHITE PIXELS " + wbHist[0]);
		// System.out.println("BLACK PIXELS " + wbHist[1]);

		for (int i = 0; i < BANDS; i++) {
			if (logoHHist[i] != 0) {
				if (hHist[i] != 0)
					colorCount += 1;
			} else {
				hHist[i] = 0;
			}
		}

		if (logoWBHist[0] != 0) {
			if (wbHist[0] != 0)
				colorCount += 1;
		} else
			wbHist[0] = 0;

		if (logoWBHist[1] != 0) {
			if (wbHist[1] != 0)
				colorCount += 1;
		} else
			wbHist[1] = 0;

		totalCountGrid = calcTotalPixels(hHist, wbHist);
		// System.out.println("\nHISTOGRAM OF GRID AFTER:");
		for (int i = 0; i < BANDS; i++) {
			// System.out.print("(" + i + ": " + hHist[i] + ")\t");
		}
		// System.out.println("WHITE PIXELS " + wbHist[0]);
		// System.out.println("BLACK PIXELS " + wbHist[1]);
		// System.out.println("TOTAL PIXEL COUNT: " + TOTAL_PIXELS_LOGO +
		// "\tPixel Count: " + totalCountGrid);
		// System.out.println("TOTAL COLOR COUNT: " + TOTAL_COLORS_LOGO +
		// "\tColor Count: " + colorCount);

		if (colorCount > Math.round(0.5 * TOTAL_COLORS_LOGO) && (totalCountGrid > 2430)) {
			for (int i = 0; i < BANDS; i++) {
				if (hHist[i] != 0 && logoHHist[i] != 0)
					gridBasedLogoPixels += logoHHist[i];
			}
			if (wbHist[0] != 0 && logoWBHist[0] != 0)
				gridBasedLogoPixels += logoWBHist[0];
			if (wbHist[1] != 0 && logoWBHist[1] != 0)
				gridBasedLogoPixels += logoWBHist[1];

			scaleFactor = (float) gridBasedLogoPixels / totalCountGrid;
			// System.out.println("\nGRID BASED LOGO PIXELS: " +
			// gridBasedLogoPixels);
			// System.out.println("SCALE FACTOR: " + scaleFactor);

			for (int i = 0; i < BANDS; i++) {
				if (hHist[i] != 0)
					hHist[i] = Math.round(hHist[i] * scaleFactor);
			}
			wbHist[0] = Math.round(wbHist[0] * scaleFactor);
			wbHist[1] = Math.round(wbHist[1] * scaleFactor);

			// System.out.println("\nSCALED HISTOGRAM:");
			for (int i = 0; i < BANDS; i++) {
				// System.out.print("(" + i + ": " + hHist[i] + ")\t");
			}
			// System.out.print("(WHITE" + ": " + wbHist[0] + ")\t");
			// System.out.print("(BLACK" + ": " + wbHist[1] + ")\t");

			newPixelCount = calcTotalPixels(hHist, wbHist);
			// System.out.println("NEW PIXEL COUNT IS " + newPixelCount);

			entropyGrid = calcEntropy(hHist, wbHist, newPixelCount);
			// System.out.println("\nENTROPY OF GRID: " + entropyGrid);

			entropyLogo = 0;
			for (int i = 0; i < BANDS; i++) {
				if (hHist[i] != 0 && logoHHist[i] != 0) {
					prob = (float) logoHHist[i] / gridBasedLogoPixels;
					if (prob != 0.0)
						entropyLogo += (prob * (Math.log(prob) / Math.log(2)));
				}
			}
			prob = (float) logoWBHist[0] / gridBasedLogoPixels;
			if (prob != 0.0 && wbHist[0] != 0 && logoWBHist[0] != 0)
				entropyLogo += (prob * (Math.log(prob) / Math.log(2)));
			prob = (float) logoWBHist[1] / gridBasedLogoPixels;
			if (prob != 0.0 && wbHist[0] != 0 && logoWBHist[0] != 0)
				entropyLogo += (prob * (Math.log(prob) / Math.log(2)));
			entropyLogo *= -1;
			// System.out.println("\nENTROPY OF LOGO: " + entropyLogo);

			proportionalizeHistogram(hHist, wbHist, proportionsH, proportionsWb, newPixelCount);

			// System.out.println("\nSCALED HISTOGRAM PROPORTIONS:");
			for (int i = 0; i < BANDS; i++) {
				// System.out.print("(" + i + ": " + proportionsH[i] + ")\t");
			}
			// System.out.print("(WHITE" + ": " + proportionsWb[0] + ")\t");
			// System.out.print("(BLACK" + ": " + proportionsWb[1] + ")\t");

			entropyMatch = piShapeFuncValue(0, entropyLogo - entropyLogo / 1000, entropyLogo + entropyLogo / 1000,
					entropyLogo * 2, entropyGrid);
			// System.out.println("ENTROPY MATCH - " + entropyMatch);

			// System.out.println("\nMATCHINGS FOR SCALED HISTOGRAM
			// PROPORTIONS:");
			for (int i = 0; i < BANDS; i++) {
				proportionHMatches[i] = piShapeFuncValue(0, logoProportionsHHist[i] - logoProportionsHHist[i] / 1000,
						logoProportionsHHist[i] + logoProportionsHHist[i] / 1000, logoProportionsHHist[i] * 2,
						proportionsH[i]);
				// System.out.print("(" + i + ": " + proportionHMatches[i] +
				// ")\t");
			}
			proportionWbMatches[0] = piShapeFuncValue(0, logoProportionsWBHist[0] - logoProportionsWBHist[0] / 1000,
					logoProportionsWBHist[0] + logoProportionsWBHist[0] / 1000, logoProportionsWBHist[0] * 2,
					proportionsWb[0]);
			// System.out.print("(WHITE" + ": " + proportionWbMatches[0] +
			// ")\t");

			proportionWbMatches[1] = piShapeFuncValue(0, logoProportionsWBHist[1] - logoProportionsWBHist[1] / 1000,
					logoProportionsWBHist[1] + logoProportionsWBHist[1] / 1000, logoProportionsWBHist[1] * 2,
					proportionsWb[1]);
			// System.out.print("(BLACK: " + proportionWbMatches[1] + ")\t");

			paramountConfidence = entropyMatch;

			for (int i = 0; i < BANDS; i++) {
				if (proportionHMatches[i] > 0)
					paramountConfidence = (paramountConfidence * proportionHMatches[i]);
			}
			if (proportionWbMatches[0] > 0)
				paramountConfidence = (paramountConfidence * proportionWbMatches[0]);
			if (proportionWbMatches[1] > 0)
				paramountConfidence = (paramountConfidence * proportionWbMatches[1]);

			// System.out.println("PARAMOUNT CONFIDENCE - " +
			// paramountConfidence);
			if (paramountConfidence >= 0.5)
				return paramountConfidence;
			else
				return 0;
		} else {
			for (int i = 0; i < BANDS; i++) {
				quoHHist[i] = 0;
			}
			quoWbHist[0] = 0;
			quoWbHist[1] = 0;
		}

		return 0;
	}
}