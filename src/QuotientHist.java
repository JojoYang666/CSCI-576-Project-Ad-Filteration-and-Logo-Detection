import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import colorConvert.ColorSpaceConverter;

public class QuotientHist {
	private static int BYTES_PER_FRAME = 388800;
	private static int FRAME_NO = 3569;
	private static int WIDTH = 480, HEIGHT = 270;
	private static String videoFile, logoFile;
	private static RandomAccessFile videoRand;
	private static long videoRandLen;
	private static RandomAccessFile logoRand;
	private static long logoRandLen;
	private static int[][] vidL = new int[WIDTH][HEIGHT];
	private static int[][] vidA = new int[WIDTH][HEIGHT];
	private static int[][] vidB = new int[WIDTH][HEIGHT];
	private static int[][] logoL = new int[WIDTH][HEIGHT];
	private static int[][] logoA = new int[WIDTH][HEIGHT];
	private static int[][] logoB = new int[WIDTH][HEIGHT];
	private static int[] vidLHist = new int[101];
	private static int[] logoLHist = new int[101];
	private static int[] vidAHist = new int[257];
	private static int[] logoAHist = new int[257];
	private static int[] vidBHist = new int[257];
	private static int[] logoBHist = new int[257];
	private static int[] quotientLHist = new int[101];
	private static int[] quotientAHist = new int[257];
	private static int[] quotientBHist = new int[257];
	private static int[][] vidLCorrected = new int[WIDTH][HEIGHT];
	private static int[][] vidACorrected = new int[WIDTH][HEIGHT];
	private static int[][] vidBCorrected = new int[WIDTH][HEIGHT];
	private static ArrayList<Integer> pixelPosL = new ArrayList<Integer>();
	private static ArrayList<Integer> pixelPosA = new ArrayList<Integer>();
	private static ArrayList<Integer> pixelPosB = new ArrayList<Integer>();
	private static ArrayList<Integer> pixelPos = new ArrayList<Integer>();

	public static void main(String[] args) {

		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				logoL[x][y] = -200;
				logoA[x][y] = -200;
				logoB[x][y] = -200;
			}
		}

		videoFile = args[0];
		logoFile = args[1];
		openVideo(videoFile);
		openLogo(logoFile);
		readFile(FRAME_NO, videoRand, vidL, vidA, vidB, false);
		readFile(0, logoRand, logoL, logoA, logoB, true);
		makeHist(vidL, vidLHist, 101, false);
		makeHist(logoL, logoLHist, 101, true);
		makeHist(vidA, vidAHist, 257, false);
		makeHist(logoA, logoAHist, 257, true);
		makeHist(vidB, vidBHist, 257, false);
		makeHist(logoB, logoBHist, 257, true);
		makeQuotient(vidLHist, logoLHist, quotientLHist, 101);
		makeQuotient(vidAHist, logoAHist, quotientAHist, 257);
		makeQuotient(vidBHist, logoBHist, quotientBHist, 257);
		getCorrectedImage(quotientLHist, vidLCorrected, vidL, 101);
		getCorrectedImage(quotientAHist, vidACorrected, vidA, 257);
		getCorrectedImage(quotientBHist, vidBCorrected, vidB, 257);
		int sum = 0;
		for (int i = 0; i < 101; i++) {
			sum += logoLHist[i];
			System.out.println("L val = " + i + ": " + logoLHist[i]);
		}

		System.out.println("Sum:" + sum);
		try {
			PrintStream output = new PrintStream(new File("output.txt"));
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					output.print(vidLCorrected[x][y]);
					output.print(" ");
					if (vidLCorrected[x][y] != 0) {
						pixelPosL.add(x);
						pixelPosL.add(y);
					}
					if (vidACorrected[x][y] != 0) {
						pixelPosA.add(x);
						pixelPosA.add(y);
					}
					if (vidBCorrected[x][y] != 0) {
						pixelPosB.add(x);
						pixelPosB.add(y);
					}
					if (vidLCorrected[x][y] != 0 || vidACorrected[x][y] != 0 || vidBCorrected[x][y] != 0) {
						pixelPos.add(x);
						pixelPos.add(y);
					}
				}
				output.println();
			}
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// for (int i = 0; i < pixelPos.size(); i += 2) {
		// System.out.println("(x,y): (" + pixelPos.get(i) + ", " +
		// pixelPos.get(i + 1) + ")");
		// }

		makeFrame(pixelPosL, '1');
		makeFrame(pixelPosA, '2');
		makeFrame(pixelPosB, '3');
		makeFrame(pixelPos, '4');
	}

	public static void openVideo(String fileName) {
		try {
			videoRand = new RandomAccessFile(fileName, "r");
			videoRandLen = videoRand.length();
		} catch (IOException e) {
			Utilities.die("File not found - " + fileName);
		}
	}

	public static void openLogo(String fileName) {
		try {
			logoRand = new RandomAccessFile(fileName, "r");
			logoRandLen = logoRand.length();
		} catch (IOException e) {
			Utilities.die("File not found - " + fileName);
		}
	}

	public static void readFile(int offset, RandomAccessFile randFile, int[][] L, int[][] A, int[][] B,
			boolean toCrop) {
		byte[] bytes = new byte[BYTES_PER_FRAME];
		int[] ans = new int[3];
		int h, w, sh, sw;
		try {
			randFile.skipBytes((int) (offset * BYTES_PER_FRAME));
			randFile.read(bytes, 0, BYTES_PER_FRAME);

			int ind = 0;

			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {

					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind + HEIGHT * WIDTH];
					byte b = bytes[ind + HEIGHT * WIDTH * 2];
					ind++;

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					int Bl = (pix) & 0xff;
					int G = (pix >> 8) & 0xff;
					int R = (pix >> 16) & 0xff;

					ans = ColorSpaceConverter.converter(R, G, Bl);
					L[x][y] = ans[0];
					A[x][y] = ans[1];
					B[x][y] = ans[2];
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// randFile.seek(0);
	}

	public static void makeHist(int[][] channelVals, int[] histogram, int len, boolean isLogo) {
		for (int y = 0; y < len; y++)
			histogram[y] = 0;

		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				if (len == 101)
					histogram[channelVals[x][y]]++;
				else
					histogram[channelVals[x][y] + 128]++;

			}
		}
	}

	public static void makeQuotient(int[] vidHist, int[] logoHist, int[] quotientHist, int len) {
		for (int i = 0; i < len; i++) {
			if (vidHist[i] != 0)
				quotientHist[i] = logoHist[i] / vidHist[i];
			else
				quotientHist[i] = 0;
		}
	}

	public static void getCorrectedImage(int[] quotientHist, int[][] vidCorrected, int[][] vid, int len) {
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				if (len == 101)
					vidCorrected[x][y] = quotientHist[vid[x][y]];
				else
					vidCorrected[x][y] = quotientHist[vid[x][y] + 128];
			}
		}
	}

	public static void makeFrame(ArrayList<Integer> pixelPos, char suffix) {
		byte[] frame = new byte[388800];

		for (int i = 0; i < BYTES_PER_FRAME; i++)
			frame[i] = 0;

		for (int i = 0; i < pixelPos.size(); i += 2) {
			frame[(pixelPos.get(i + 1) * WIDTH) + pixelPos.get(i)] = -127;
			frame[(pixelPos.get(i + 1) * WIDTH) + pixelPos.get(i) + (WIDTH * HEIGHT)] = -127;
			frame[(pixelPos.get(i + 1)) + pixelPos.get(i) + (WIDTH * HEIGHT * 2)] = -127;
		}

		try {
			FileOutputStream fos = new FileOutputStream("frame" + suffix + ".rgb");
			fos.write(frame);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}