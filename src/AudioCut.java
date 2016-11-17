import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AudioCut
{
  final private static String INPUT_FILE = "../../dataset/Videos/data_test1.wav";
  final private static int HEADER_SIZE = 44;
  final private static int SAMPLES_PER_FRAME = 1600;
  
  private static ArrayList<Shot> shots;
  private static DataInputStream dis;
  private static double[] rmses;
  private static int bytesPerFrame = 2 * SAMPLES_PER_FRAME;
  
  private static void findOutliers()
  {
    double sumRMS = 0;
    for (double rms: rmses)
    {
      sumRMS += rms;
    }
    double averageRMS = sumRMS / rmses.length;
    double sumRMSDifference = 0;
    for (double rms: rmses)
    {
      sumRMSDifference += Math.abs(rms - averageRMS);
    }
    double max = averageRMS + (4 * (sumRMSDifference / rmses.length));
    double min = averageRMS - (4 * (sumRMSDifference / rmses.length));
    System.out.println("Max: " + max + " Min: " + min);
    for (int i = 0; i < rmses.length; i++)
    {
      if (rmses[i] > max)
      {
        System.out.println("RMS" + i + " is out of threshold");
        //System.out.println("max: " + rmses[i] + " " + max);
      }
      else if (rmses[i] < min)
      {
        System.out.println("RMS" + i + " is out of threshold");
        //System.out.println("min: " + rmses[i] + " " + min);
      }
    }
  }
  
  private static void makeShots()
  {
    shots = new ArrayList<Shot>();
    shot = new Shot();
    
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(235612800);
    shot.setLengthOfShot(222782400);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(458395200);
    shot.setLengthOfShot(474724800);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(933120000);
    shot.setLengthOfShot(13608000);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(946728000);
    shot.setLengthOfShot(20995200);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(0);
    shot.setLengthOfShot(235612800);
    shots.add(shot);
  }
  
  private static void openFile()
  {
    try
    {
      File file = new File(INPUT_FILE);
      InputStream inputFileStream = new BufferedInputStream(new
        FileInputStream(file));
      dis = new DataInputStream(inputFileStream);
      
      double dataSize = (double)(file.length() - HEADER_SIZE);
      double arraySize = Math.ceil(dataSize / bytesPerFrame);
      rmses = new double[(int)arraySize];
    }
    catch (FileNotFoundException e) {Utilities.die("FileNotFound");}
  }
  
  private static void readData()
  {
    try
    {
      dis.skip(44);
      
      int index = 0;
      int bytesToRead = dis.available();
      while (bytesToRead != 0)
      {
        if (bytesToRead > bytesPerFrame)
        {
          bytesToRead = bytesPerFrame;
        }
        int n = 0;
        double sum = 0;
        for (int i = 0; i < (bytesToRead / 2); i++)
        {
          int amp = dis.readShort();
          sum += (amp * amp);
          n++;
        }
        rmses[index++] = Math.sqrt(sum/n);
        bytesToRead = dis.available();
      }
    }
    catch (IOException e) {Utilities.die("IOException");}
  }
  
  public static void main(String[] args)
  {
    makeShots();
    openFile();
    readData();
    findOutliers();
  }
}
