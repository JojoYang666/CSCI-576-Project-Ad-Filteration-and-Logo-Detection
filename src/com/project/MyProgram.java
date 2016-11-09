package com.project;

import java.util.ArrayList;

public class MyProgram{
	private ArrayList<Shot> shots;
	private double globalYMean;
	private double globalMseAmp;
	private ArrayList<Scene> scenes;
	private static String inputVideoFile, inputAudioFile, outputVideoFile, outputAudioFile;
	
	public static void main(String[] args){
		parseArgs(args);
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