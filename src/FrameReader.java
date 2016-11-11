

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


public class FrameReader {

	private static InputStream inputFileStream;
	//private static BufferedInputStream bufferedInputFileStream;
	private static RandomAccessFile randFile;
	private static long fileLength;
	private static int width, height, len;
	//private static double[][] yMatrix;
    private static YUV yuv;
	
	public FrameReader(String fileName, int width, int height){
		this.width = width;
		this.height = height;
		this.len = width*height*3;
		//this.yMatrix = new double[width][height];
        this.yuv = null;
		open(fileName);
	}
	/**
	 * Reading the RGB value of a one frame and returns Y values 
	 * @param offset frame location where Y value is going to be calculated. 
	 * @return double 2D matrix that is Y value of one frame
	 */
	public YUV read(long offset){

		double[][] yMatrix = new double[width][height];
        double[][] uMatrix = new double[width][height];  // Pb here
        double[][] vMatrix = new double[width][height];  // Pr here
      
        try {
			byte[] bytes = new byte[len];
			/*bufferedInputFileStream.skip(offset*len);
			bufferedInputFileStream.read(bytes, 0, len);
			bufferedInputFileStream.reset();*/
			
			randFile.skipBytes((int) (offset*len));
			randFile.read(bytes, 0, len);
			randFile.seek(0);

			int ind = 0;
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){

					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 
					ind++;
					yMatrix[x][y] = trimY((0.299*(int)(r+128) + 0.587*(int)(g+128) + 0.114*(int)(b+128)));
                    uMatrix[x][y] = trimUV((-0.169*(int)(r+128) + -0.331*(int)(g+128) + 0.5*(int)(b+128)));
                    vMatrix[x][y] = trimUV((0.5*(int)(r+128) + -0.419*(int)(g+128) + -0.081*(int)(b+128)));
					//System.out.println(yMatrix[x][y]);
				}
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.yuv = new YUV(yMatrix, uMatrix, vMatrix);
        return(this.yuv);
	}
	/**
	 * Open file 
	 * @param fileName File name 
	 */
	public static void open(String fileName){
		try {
			/*File file = new File(fileName);
			inputFileStream = new FileInputStream(file);
			bufferedInputFileStream = new BufferedInputStream(inputFileStream);*/
			randFile = new RandomAccessFile(fileName, "r");
			fileLength = randFile.length();
		} catch (IOException e) {
			Utilities.die("File not found - " + fileName);
		}
	}
	/**
	 * close the input file stream. 
	 */
	public static void close(){
		try {
			randFile.close();
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
		FrameReader.inputFileStream = inputFileStream;
	}

	public static int getWidth() {
		return width;
	}

	public static void setWidth(int width) {
		FrameReader.width = width;
	}

	public static int getHeight() {
		return height;
	}

	public static void setHeight(int height) {
		FrameReader.height = height;
	}

	public static int getLen() {
		return len;
	}

	public static void setLen(int len) {
		FrameReader.len = len;
	}
  
    public static YUV getYUV() {
        return yuv;
    }

	public static void setFileLength(long fileLength) {
		FrameReader.fileLength = fileLength;
	}

	public int getNumberOfFrames(){
		return (int)(fileLength/len);
	}
  
    private static double trimUV(double num)
    {
        if (num < -127.5) return(-127.5);
        if (num > 127.5) return(127.5);
        return(num);
    }

    private static double trimY(double num)
    {
        if (num < 0) return(0);
        if (num > 255) return(255);
        return(num);
    }
}