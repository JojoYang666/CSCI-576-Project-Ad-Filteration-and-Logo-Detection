package com.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class FrameReader {

	private static InputStream inputFileStream;
	private static long fileLength;
	private static int width, height, len;
	private static double[][] yMatrix;
	
	public FrameReader(String fileName, int width, int height){
		this.width = width;
		this.height = height;
		this.len = width*height*3;
		this.yMatrix = new double[width][height];
		open(fileName);
	}
	
	public double[][] read(long offset){

		try {
			byte[] bytes = new byte[len];
			inputFileStream.read(bytes, (int)offset*len, len);

			int ind = 0;
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){

					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 
					ind++;
					yMatrix[y][x] = (0.299*(int)r + 0.587*(int)g + 0.114*(int)b);
				}
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return yMatrix;
	}
	
	public static void open(String fileName){
		try {
			File file = new File(fileName);
			inputFileStream = new FileInputStream(file);
			fileLength = file.length();
		} catch (FileNotFoundException e) {
			Utilities.die("File not found - " + fileName);
		}
	}
	
	public static void close(){
		try {
			inputFileStream.close();
		} catch (IOException e) {
			Utilities.die("IO Exception - RGB File");
			e.printStackTrace();
		}
	}
	
	public static long getFileLength(){
		return fileLength;
	}

}