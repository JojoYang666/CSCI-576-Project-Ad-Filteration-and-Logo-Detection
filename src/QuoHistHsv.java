import java.awt.Color;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class QuoHistHsv {
	private static int BYTES_PER_FRAME = 388800;
	private static int FRAME_NO = 4359;
	private static int GRID_SIZE = 90;
	private static int H_SIZE = 361;
	private static int LIMIT = 1;
	private static int TOTAL_PIXELS_LOGO;
	private static int TOTAL_COLORS_LOGO;
	private static float LOGO_ENTROPY;
	private static int WIDTH = 480, HEIGHT = 270;
	private static String videoFile, logoFile;
	private static RandomAccessFile videoRand;
	private static RandomAccessFile logoRand;
	private static int[][] vidH = new int[WIDTH][HEIGHT];
	private static int[][] vidS = new int[WIDTH][HEIGHT];
	private static int[][] vidV = new int[WIDTH][HEIGHT];
	private static int[][] vidFilteredH = new int[WIDTH][HEIGHT];
	private static int[][] logoH = new int[WIDTH][HEIGHT];
	private static float[] logoProportionsHHist = new float[H_SIZE];
	private static int[][] logoS = new int[WIDTH][HEIGHT];
	private static int[][] logoV = new int[WIDTH][HEIGHT];
	private static int[] vidHHist = new int[H_SIZE];
	private static float[] vidProportionsHHist = new float[H_SIZE];
	private static int[] logoHHist = new int[H_SIZE];
	private static int[] vidWBHist = new int[2];
	private static float[] vidWBProportionsHist = new float[2];
	private static int[] logoWBHist = new int[2];
	private static float[] logoProportionsWBHist = new float[2];
	private static float[] quotientHHist = new float[H_SIZE];
	private static float[] quotientWBHist = new float[2];
	private static int[][] vidCorrected = new int[WIDTH][HEIGHT];
	private static boolean logoPresent = false;
	private static float paramountConfidence;
	// private static int[][] gaussianFilter = { { 1, 2, 1 }, { 2, 4, 2 }, { 1,
	// 2, 1 } };

	public static void main(String[] args) {

		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				logoH[x][y] = -200;
				logoS[x][y] = -200;
				logoV[x][y] = -200;
			}
		}

		videoFile = args[0];
		logoFile = args[1];
		openVideo(videoFile);
		openLogo(logoFile);
		readFile(FRAME_NO, videoRand, vidH, vidS, vidV);
		readFile(0, logoRand, logoH, logoS, logoV);
		filterFrame(vidH, vidFilteredH);
		// filterFrame(vidH, vidFilteredH);
		// filterFrame(vidS);
		// filterFrame(vidV);

		makeHistogramForLogo(logoH, logoS, logoV, logoHHist, logoWBHist);
		System.out.println("HISTOGRAM OF LOGO AFTER:");
		for (int i = 0; i < H_SIZE; i++) {
			System.out.print("(" + i + ": " + logoHHist[i] + ")\t");
		}
		System.out.println("WHITE PIXELS - " + logoWBHist[0]);
		System.out.println("BLACK PIXELS - " + logoWBHist[1] + "\n\n");

		normalizeHistogramForLogo(logoHHist, logoWBHist);
		TOTAL_PIXELS_LOGO = calcTotalPixels(logoHHist, logoWBHist);
		TOTAL_COLORS_LOGO = calcTotalColors(logoHHist, logoWBHist);
		LOGO_ENTROPY = calcEntropy(logoHHist, logoWBHist, TOTAL_PIXELS_LOGO);
		proportionalizeHistogram(logoHHist, logoWBHist, logoProportionsHHist, logoProportionsWBHist, TOTAL_PIXELS_LOGO);
		System.out.println("LOGO ENTROPY - " + LOGO_ENTROPY);
		System.out.println("TOTAL PIXELS: " + TOTAL_PIXELS_LOGO);
		System.out.println("TOTAL COLORS: " + TOTAL_COLORS_LOGO);

		System.out.println("HISTOGRAM OF LOGO AFTER:");
		for (int i = 0; i < H_SIZE; i++) {
			System.out.print("(" + i + ": " + logoProportionsHHist[i] + ")\t");
		}
		System.out.println("WHITE PIXELS - " + logoProportionsWBHist[0]);
		System.out.println("BLACK PIXELS - " + logoProportionsWBHist[1]);

		for (int y = 0; y < HEIGHT; y += GRID_SIZE) {
			for (int x = 0; x < WIDTH; x += GRID_SIZE) {
				System.out.println(x + " " + y);
				makeHistogramForFrame(vidFilteredH, vidS, vidV, vidHHist, vidWBHist, x, y);
				getQuotientHist(vidHHist, vidWBHist, quotientHHist, quotientWBHist, vidProportionsHHist,
						vidWBProportionsHist);
				getCorrectedImg(vidCorrected, vidFilteredH, vidS, vidV, quotientHHist, quotientWBHist, x, y);
			}
		}

		// for (int y = 0; y < HEIGHT; y++) {
		// for (int x = 0; x < WIDTH; x++) {
		// if (vidCorrected[x][y] != 0) {
		// pixelPosH.add(x);
		// pixelPosH.add(y);
		// }
		// }
		// }
		makeFrame(vidCorrected, '1');
		System.out.println("\n\n\nCORRECTED IMAGE");
		int energy, max_energy = -Integer.MAX_VALUE, limy, limx, gx = 0, gy = 0;

		if (logoPresent == true) {
			System.out.println("IN THIS DEEP ONE");
			for (int y = 0; y < HEIGHT; y += GRID_SIZE) {
				for (int x = 0; x < WIDTH; x += GRID_SIZE) {

					limy = y + GRID_SIZE;
					limx = x + GRID_SIZE;

					if (limy > HEIGHT)
						limy = HEIGHT;
					if (limx > WIDTH)
						limx = WIDTH;

					energy = 0;
					for (int j = y; j < limy; j++) {
						for (int i = x; i < limx; i++) {
							energy += vidCorrected[i][j];
						}
					}

					if (energy > max_energy) {
						max_energy = energy;
						gx = x;
						gy = y;
					}

				}
			}
		} else
			System.out.println("NO LOGOS, BRO!");

		if (logoPresent == true)
			System.out.println("MAX ENERGY IN GRID: " + gx + "\t" + gy);
	}

	private static void proportionalizeHistogram(int[] hHist, int[] wbHist, float[] proportionsH, float[] proportionsWb,
			int totalPixels) {
		for (int i = 0; i < H_SIZE; i++) {
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

		// System.out.println("### FILTERED HUES ###");
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				// System.out.print(x + "\t" + y + "\tBEFORE: " + H[x][y] +
				// "\t");
				ind = 0;
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						if ((x + i) < 0 || (x + i) >= WIDTH)
							continue;
						if ((y + j) < 0 || (y + j) >= HEIGHT)
							continue;

						// sum += H[x + i][y + j];
						// count += 1;
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
				// System.out.println("AFTER: " + filteredH[x][y]);
			}
		}
	}

	private static void normalizeHistogramForLogo(int[] lgH, int[] lgWb) {
		int avg = 0, colors = 0, i;
		ArrayList<Integer> hue = new ArrayList<Integer>();

		for (int k = 0; k < H_SIZE; k++) {
			if (lgH[k] != 0) {
				avg += lgH[k];
				colors += 1;
				hue.add(k);
			}
		}
		if (lgWb[0] != 0) {
			avg += lgWb[0];
			colors += 1;
		}

		if (lgWb[1] != 0) {
			avg += lgWb[1];
			colors += 1;
		}

		avg = avg / colors;
		avg = avg / (LIMIT * 2);

		for (int k = 0; k < hue.size(); k++) {
			i = hue.get(k);
			if (lgH[i] != 0) {

				if (i < LIMIT) {
					for (int j = i + 1; j <= (i + LIMIT); j++) {
						if (lgH[j] == 0)
							lgH[j] = avg;
					}
					for (int j = 0; j < i; j++) {
						if (lgH[j] == 0)
							lgH[j] = avg;
					}
					for (int j = (H_SIZE - (LIMIT - i)); j < H_SIZE; j++) {
						if (lgH[j] == 0)
							lgH[j] = avg;
					}
				} else if (i >= (H_SIZE - LIMIT)) {
					for (int j = (i - LIMIT); j < i; j++) {
						if (lgH[j] == 0)
							lgH[j] = avg;
					}
					for (int j = i + 1; j < H_SIZE; j++) {
						if (lgH[j] == 0)
							lgH[j] = avg;
					}
					for (int j = 0; j < (i - (H_SIZE - LIMIT + 1)); j++) {
						if (lgH[j] == 0)
							lgH[j] = avg;
					}
				} else {
					for (int j = i + 1; j <= (i + LIMIT); j++) {
						if (lgH[j] == 0)
							lgH[j] = avg;
					}
					for (int j = (i - LIMIT); j < i; j++) {
						if (lgH[j] == 0)
							lgH[j] = avg;
					}
				}
			}
		}
	}

	private static int calcTotalColors(int[] hHist, int[] wbHist) {
		int ans = 0;

		for (int i = 0; i < H_SIZE; i++) {
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

		for (int i = 0; i < H_SIZE; i++)
			ans += hHist[i];
		ans += wbHist[0] + wbHist[1];

		return ans;
	}

	private static float calcEntropy(int[] hHist, int[] wbHist, int totalCount) {
		float ans = 0, prob;

		for (int i = 0; i < H_SIZE; i++) {
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
		float[] hsbVals = new float[3];
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

					Color.RGBtoHSB(R, G, Bl, hsbVals);
					H[x][y] = Math.round(hsbVals[0] * 360);
					S[x][y] = Math.round(hsbVals[1] * 100);
					V[x][y] = Math.round(hsbVals[2] * 100);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// randFile.seek(0);
	}

	public static void makeHistogramForLogo(int[][] H, int[][] S, int[][] V, int[] hHist, int[] wbHist) {
		boolean beg = false, end = false;
		int begPosX = 0, endPosX = 0;
		int mostCommonH, mostCommonS, mostCommonV;

		mostCommonH = H[2][2];
		mostCommonS = S[2][2];
		mostCommonV = V[2][2];

		for (int i = 0; i < H_SIZE; i++)
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
					// System.out.println("X: " + begPosX + " Y: " + y + " X: "
					// + endPosX + " Y: " + y);
					for (int i = begPosX; i <= endPosX; i++) {
						// Check for white color
						if (V[i][y] >= 90 && V[i][y] <= 100 && S[i][y] >= 0 && S[i][y] <= 10)
							wbHist[0]++;
						else if (V[i][y] >= 0 && V[i][y] <= 10 && S[i][y] >= 0 && S[i][y] <= 10)
							wbHist[1]++;
						else
							hHist[H[i][y]]++;
					}
					break;
				}
			}
		}
	}

	private static void makeHistogramForFrame(int[][] H, int[][] S, int[][] V, int[] hHist, int[] wbHist, int wx,
			int hy) {
		for (int i = 0; i < H_SIZE; i++)
			hHist[i] = 0;
		wbHist[0] = 0;
		wbHist[1] = 0;

		int limy = hy + GRID_SIZE, limx = wx + GRID_SIZE;
		if (limy >= HEIGHT)
			limy = HEIGHT;
		if (limx >= WIDTH)
			limx = WIDTH;

		for (int y = hy; y < limy; y++) {
			for (int x = wx; x < limx; x++) {
				if (V[x][y] >= 90 && V[x][y] <= 100 && S[x][y] >= 0 && S[x][y] <= 10)
					wbHist[0]++;
				else if (V[x][y] >= 0 && V[x][y] <= 10 && S[x][y] >= 0 && S[x][y] <= 10)
					wbHist[1]++;
				else {
					hHist[H[x][y]]++;
				}
			}
		}
	}

	private static void getQuotientHist(int[] hHist, int[] wbHist, float[] quoHHist, float[] quoWbHist,
			float[] proportionsH, float[] proportionsWb) {
		int pixelCount = 0, colorCount = 0, totalCountGrid;
		float entropyGrid = 0, maxi, mini;
		int counter;
		// boolean countOutliers = false;
		// ArrayList<Integer> notUsed = new ArrayList<Integer>();

		System.out.println("HISTOGRAM OF GRID BEFORE:");
		for (int i = 0; i < H_SIZE; i++) {
			System.out.print("(" + i + ": " + hHist[i] + ")\t");
		}
		System.out.println("WHITE PIXELS " + wbHist[0]);
		System.out.println("BLACK PIXELS " + wbHist[1]);
		for (int i = 0; i < H_SIZE; i++) {
			if (logoHHist[i] != 0) {

				pixelCount += hHist[i];
				if (hHist[i] != 0)
					colorCount += 1;
			} else {
				hHist[i] = 0;
			}
		}

		if (logoWBHist[0] != 0) {
			pixelCount += wbHist[0];
			if (wbHist[0] != 0)
				colorCount += 1;
		} else
			wbHist[0] = 0;

		if (logoWBHist[1] != 0) {
			pixelCount += wbHist[1];
			if (wbHist[1] != 0)
				colorCount += 1;
		} else
			wbHist[1] = 0;

		// for (int i = 0; i < notUsed.size(); i++) {
		// hHist[notUsed.get(i)] = 0;
		// }

		System.out.println("HISTOGRAM OF GRID AFTER:");
		for (int i = 0; i < H_SIZE; i++) {
			System.out.print("(" + i + ": " + hHist[i] + ")\t");
		}
		System.out.println("WHITE PIXELS " + wbHist[0]);
		System.out.println("BLACK PIXELS " + wbHist[1]);
		System.out.println("TOTAL PIXEL COUNT: " + TOTAL_PIXELS_LOGO + "\tPixel Count: " + pixelCount);
		System.out.println("TOTAL COLOR COUNT: " + TOTAL_COLORS_LOGO + "\tColor Count: " + colorCount);

		if (colorCount >= Math.round(0.3 * TOTAL_COLORS_LOGO)) {
			// Need to calculate entropy
			totalCountGrid = calcTotalPixels(hHist, wbHist);
			entropyGrid = calcEntropy(hHist, wbHist, totalCountGrid);
			System.out.println("ENTROPY OF GRID: " + entropyGrid);

			if (Math.abs(entropyGrid - LOGO_ENTROPY) < (0.1 * LOGO_ENTROPY)) {
				System.out.println("IN THE MATRIX");
				if (logoPresent == false)
					logoPresent = true;

				paramountConfidence = 0;
//				counter = 0;
				proportionalizeHistogram(hHist, wbHist, proportionsH, proportionsWb, totalCountGrid);

				for (int i = 0; i < H_SIZE; i++) {
					if (hHist[i] != 0)
						quoHHist[i] = piShapeFuncValue(0, logoProportionsHHist[i], logoProportionsHHist[i], 1,
								proportionsH[i]);
					else
						quoHHist[i] = 0;
				}

				if (wbHist[0] != 0)
					quoWbHist[0] = piShapeFuncValue(0, logoProportionsWBHist[0], logoProportionsWBHist[0], 1,
							proportionsWb[0]);
				else
					quoWbHist[0] = 0;

				if (wbHist[1] != 0)
					quoWbHist[1] = piShapeFuncValue(0, logoProportionsWBHist[1], logoProportionsWBHist[1], 1,
							proportionsWb[1]);
				else
					quoWbHist[1] = 0;

				maxi = -Float.MAX_VALUE;
				mini = Float.MAX_VALUE;

				for (int i = 0; i < H_SIZE; i++) {
					maxi = Math.max(quoHHist[i], maxi);
					mini = Math.min(quoHHist[i], mini);
				}

				maxi = Math.max(quoWbHist[0], maxi);
				maxi = Math.max(quoWbHist[1], maxi);
				mini = Math.min(quoWbHist[0], mini);
				mini = Math.min(quoWbHist[1], mini);

				for (int k = 0; k < H_SIZE; k++) {
					if (quoHHist[k] != 0) {
						paramountConfidence += quoHHist[k];
						quoHHist[k] = Math.round((float) (((quoHHist[k] - mini) / (maxi - mini)) * 255));
						
//						counter++;
					}
				}

				if (quoWbHist[0] != 0) {
					paramountConfidence += quoWbHist[0];
					quoWbHist[0] = Math.round((float) (((quoWbHist[0] - mini) / (maxi - mini)) * 255));
//					counter++;
				}
				if (quoWbHist[1] != 0) {
					paramountConfidence += quoWbHist[1];
					quoWbHist[1] = Math.round((float) (((quoWbHist[1] - mini) / (maxi - mini)) * 255));
//					counter++;
				}

				System.out.println("#### QUOTIENT HISTOGRAM OF GRID: ####");
				for (int i = 0; i < H_SIZE; i++) {
					System.out.print("(" + i + ": " + quoHHist[i] + ")\t");
				}
				System.out.println("QUOTIENT WHITE PIXELS " + quoWbHist[0]);
				System.out.println("QUOTIENT BLACK PIXELS " + quoWbHist[1]);
				System.out.println("MAX_VALUE - " + maxi);
				System.out.println("MIN_VALUE - " + mini);
				System.out.println("PARAMOUNT CONFIDENCE - " + paramountConfidence);

			} else {
				for (int i = 0; i < H_SIZE; i++) {
					quoHHist[i] = 0;
				}
				quoWbHist[0] = 0;
				quoWbHist[1] = 0;
			}
		} else {
			for (int i = 0; i < H_SIZE; i++) {
				quoHHist[i] = 0;
			}
			quoWbHist[0] = 0;
			quoWbHist[1] = 0;
		}
	}

	private static void getCorrectedImg(int[][] colCorrected, int[][] H, int[][] S, int[][] V, float[] quoHHist,
			float[] quoWBHist, int wx, int hy) {

		int limy = hy + GRID_SIZE, limx = wx + GRID_SIZE;
		if (limy >= HEIGHT)
			limy = HEIGHT;
		if (limx >= WIDTH)
			limx = WIDTH;

		for (int y = hy; y < limy; y++) {
			for (int x = wx; x < limx; x++) {
				if (V[x][y] >= 90 && V[x][y] <= 100 && S[x][y] >= 0 && S[x][y] <= 10)
					colCorrected[x][y] = (int) quoWBHist[0];
				else if (V[x][y] >= 0 && V[x][y] <= 10 && S[x][y] >= 0 && S[x][y] <= 10)
					colCorrected[x][y] = (int) quoWBHist[1];
				else
					colCorrected[x][y] = (int) quoHHist[H[x][y]];
			}
		}

	}

	public static void makeFrame(int[][] vidCorr, char suffix) {
		// byte[] frame = new byte[388800];
		//
		// for (int i = 0; i < BYTES_PER_FRAME; i++)
		// frame[i] = 0;
		//
		//// for (int i = 0; i < pixelPos.size(); i += 2) {
		//// frame[(pixelPos.get(i + 1) * WIDTH) + pixelPos.get(i)] = -127;
		//// frame[(pixelPos.get(i + 1) * WIDTH) + pixelPos.get(i) + (WIDTH *
		// HEIGHT)] = -127;
		//// frame[(pixelPos.get(i + 1)) + pixelPos.get(i) + (WIDTH * HEIGHT *
		// 2)] = -127;
		//// }
		//
		// for(int y = 0; y<HEIGHT; y++){
		// for(int x = 0; x<WIDTH; x++){
		// frame[(y * WIDTH) + x] = (byte) -128;
		// frame[(y * WIDTH) + x + (WIDTH * HEIGHT)] = (byte) -128;
		// frame[y + x + (WIDTH * HEIGHT * 2)] = (byte) -128;
		// }
		// }
		//
		// try {
		// FileOutputStream fos = new FileOutputStream("frame" + suffix +
		// ".rgb");
		// fos.write(frame);
		// fos.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// BufferedImage theImage = new BufferedImage(480, 270,
		// BufferedImage.TYPE_INT_RGB);
		// for(int x = 0; x<WIDTH; x++){
		// for(int y = 0; y<HEIGHT; y++){
		// int value = vidCorr[y][x] << 16 | vidCorr[y][x] << 8 | vidCorr[y][x];
		// theImage.setRGB(x, y, value);
		// }
		// }
		// File outputfile = new File("saved.bmp");
		// try {
		// ImageIO.write(theImage, "png", outputfile);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}