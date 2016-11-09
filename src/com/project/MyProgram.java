package com.project;

import java.util.ArrayList;

public class MyProgram{
	private static ArrayList<Shot> shots;
	private double globalYMean;
	private double globalMseAmp;
	private static final int width = 470, height = 280;
	private ArrayList<Scene> scenes;
	private static String inputVideoFile, inputAudioFile, outputVideoFile, outputAudioFile;
	private static double currentYMean;
	private static double[][] yMatrix = new double[width][height];
	
	public static void main(String[] args){
		parseArgs(args);
		FrameReader fReader = new FrameReader(inputVideoFile, width, height);
		long maxNumOfFrames = fReader.getNumberOfFrames();
		int offset = 0;
		int numOfFrames = 0;
		double yFrameAvg = 0;
		boolean newShot = true;
		
		while(offset<maxNumOfFrames){
			
			if(newShot==false){
				yMatrix = fReader.read(offset);
				for(int y = 0;y<width;y+=10){
					for(int x = 0; x<height; x+=10){
						yFrameAvg += yMatrix[y][x]; 
					}
				}
				numOfFrames++;
				yFrameAvg /= ((width/10)*(height/10));
				
				if(Math.abs(yFrameAvg - currentYMean)>(0.1*currentYMean)){
					//Exceeded threshold
					newShot = true;
					shots.get(shots.size()-1).setLengthOfShot((offset*fReader.getLen() - 1) - shots.get(shots.size()-1).getStartingByte());
				}
			}
			
			if(newShot==true){
				shots.add(new Shot());
				shots.get(shots.size()-1).setStartingByte(offset*fReader.getLen());
				yMatrix = fReader.read(offset);
				for(int y = 0;y<width;y+=10){
					for(int x = 0; x<height; x+=10){
						yFrameAvg += yMatrix[y][x]; 
					}
				}
				numOfFrames++;
				yFrameAvg /= ((width/10)*(height/10));
				currentYMean += yFrameAvg;
				offset+=5;
				newShot = false;
			}
		}
		
	}
	
	private static void parseArgs(String[] args){
		if(args.length!=4){
			Utilities.die("Not enough arguments! \nUsage java MyProgram input.rgb input.wav output.rgb output.wav");		
		}
		
		inputVideoFile = args[0];
		inputAudioFile = args[1];
		outputVideoFile = args[2];
		outputAudioFile = args[3];
	}
	
}