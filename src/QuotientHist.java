import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import colorConvert.ColorSpaceConverter;

public class QuotientHist {
	private static int BYTES_PER_FRAME = 388800;
	private static int FRAME_NO = 5419;
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
	private static int[] quotientHist = new int[101];
	private static int[][] vidLCorrected = new int[WIDTH][HEIGHT];
	private static ArrayList<Integer> pixelPos = new ArrayList<Integer>();	 
	
	public static void main(String[] args) {
		videoFile = args[0];
		logoFile = args[1];
		openVideo(videoFile);
		openLogo(logoFile);
		readFile(FRAME_NO, videoRand, vidL, vidA, vidB);
		readFile(0, logoRand, logoL, logoA, logoB);
		makeHist(vidL, vidLHist, 101);
		makeHist(logoL, logoLHist, 101);
		makeQuotient(vidLHist, logoLHist, quotientHist, 101);
		getCorrectedImage(quotientHist, vidLCorrected, vidL);
		int sum = 0;
		for(int i = 0;i <101; i++){
			sum += logoLHist[i];
			System.out.println("L val = " + i + ": " + logoLHist[i]);
		}
		
		System.out.println("Sum:" + sum);
		try {
			PrintStream output = new PrintStream(new File("output.txt"));
			for(int y = 0; y<HEIGHT; y++){
				for(int x = 0; x<WIDTH; x++){
					output.print(vidLCorrected[x][y]);
					output.print(" ");
					if(vidLCorrected[x][y]!=0){
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
		
		for(int i=0;i<pixelPos.size();i+=2){
			System.out.println("(x,y): (" + pixelPos.get(i) + ", " + pixelPos.get(i+1) + ")");
		}
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

	public static void readFile(int offset, RandomAccessFile randFile, int[][] L, int[][] A, int[][] B) {
		byte[] bytes = new byte[BYTES_PER_FRAME];
		int[] ans = new int[3];
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

					ans =  ColorSpaceConverter.converter((int) (r+128), (int) (g+128), (int) (b+128));
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
	
	public static void makeHist(int[][] channelVals, int[] histogram, int len){
		for(int y = 0; y<len; y++)
			histogram[y] = 0;
		
		for(int y = 0; y<HEIGHT; y++){
			for(int x = 0; x<WIDTH; x++){
				histogram[channelVals[x][y]]++;
			}
		}
	}
	
	public static void makeQuotient(int[] vidHist, int[] logoHist, int[] quotientHist, int len){
		for(int i = 0; i<len; i++){
			if(vidHist[i]!=0)
				quotientHist[i] = logoHist[i]/vidHist[i];
			else
				quotientHist[i] = 0;
		}
	}
	
	public static void getCorrectedImage(int[] quotientHist, int[][] vidLCorrected, int[][] vidL){
		for(int y = 0; y<HEIGHT; y++){
			for(int x = 0; x<WIDTH; x++){
				vidLCorrected[x][y] = quotientHist[vidL[x][y]];
			}
		}
	}
}