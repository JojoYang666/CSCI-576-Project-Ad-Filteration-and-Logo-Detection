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
    shot.setStartingByte(967723200);
    shot.setLengthOfShot(10108800);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(977832000);
    shot.setLengthOfShot(31492800);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(1009324800);
    shot.setLengthOfShot(24494400);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(1033819200);
    shot.setLengthOfShot(13996800);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(1047816000);
    shot.setLengthOfShot(20606400);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(1068422400);
    shot.setLengthOfShot(21384000);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(1089806400);
    shot.setLengthOfShot(13608000);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(1103414400);
    shot.setLengthOfShot(4665600);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(1108080000);
    shot.setLengthOfShot(303264000);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(1411344000);
    shot.setLengthOfShot(279936000);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(1691280000);
    shot.setLengthOfShot(466560000);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(2157840000);
    shot.setLengthOfShot(12441600);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(2170281600);
    shot.setLengthOfShot(11664000);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(2181945600);
    shot.setLengthOfShot(33825600);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(2215771200);
    shot.setLengthOfShot(20995200);
    shots.add(shot);
    
    shot = new Shot();
    shot.setStartingByte(2236766400);
    shot.setLengthOfShot(36158400);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(2272924800);
    shot.setLengthOfShot(30715200);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(2303640000);
    shot.setLengthOfShot(24105600);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(2327745600);
    shot.setLengthOfShot(5054400);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(2332800000);
    shot.setLengthOfShot(174960000);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(2507760000);
    shot.setLengthOfShot(291600000);
    shots.add(shot);
    shot = new Shot();
    shot.setStartingByte(2799360000);
    shot.setLengthOfShot(699840000);
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
