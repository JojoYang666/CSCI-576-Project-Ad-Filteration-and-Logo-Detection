package com.project;

public class AudioReader(){
  
	private static InputStream inputFileStream;
	private static long fileLength;
	private static int len;
    final int samplingRate = 48000;
	
	public AudioReader(String fileName){
		open(fileName);
	}
	
	public double read(long offset){

		try {
			byte[] bytes = new byte[len];
			inputFileStream.read(bytes, (int)offset*len, len);
          
          
/*

final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
    
    
    AudioInputStream audioInputStream = null;
	try
    {

      InputStream audioBIS =
        new BufferedInputStream(new FileInputStream(args[1]));
      audioInputStream = AudioSystem.getAudioInputStream(audioBIS);
      
      
      // Obtain the information about the AudioInputStream
	AudioFormat audioFormat = audioInputStream.getFormat();
	Info info = new Info(SourceDataLine.class, audioFormat);

	// opens the audio channel
	SourceDataLine dataLine = null;
	try {
	    dataLine = (SourceDataLine) AudioSystem.getLine(info);
	    dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE);
	} catch (LineUnavailableException e1) {}

	// Starts the music :P
	dataLine.start();

	int readBytes = 0;
	byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];

	try {
	    while (readBytes != -1) {
		readBytes = audioInputStream.read(audioBuffer, 0,
			audioBuffer.length);
		if (readBytes >= 0){
		    dataLine.write(audioBuffer, 0, readBytes);
		}
	    }
	}
    catch (IOException e1) {}
    finally {
	    // plays what's left and and closes the audioChannel
	    dataLine.drain();
	    dataLine.close();
	}
      
*/      
      
      
	}
    catch (FileNotFoundException e)
    {
      die("FileNotFoundException");
    }
    catch (IOException e)
    {
      die("IOException");
	}
    catch (UnsupportedAudioFileException e)
    {
      die("UnsupportedAudioFileException");
	}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void open(String fileName){
		try {
			File file = new File(fileName);
			inputFileStream = new BufferedInputStream(new FileInputStream(file);
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
	
	public long getFileLength(){
		return fileLength;
	}

	public static InputStream getInputFileStream() {
		return inputFileStream;
	}

	public static void setInputFileStream(InputStream inputFileStream) {
		this.inputFileStream = inputFileStream;
	}

	public static int getLen() {
		return len;
	}

	public static void setLen(int len) {
		this.len = len;
	}

	public static void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}
}