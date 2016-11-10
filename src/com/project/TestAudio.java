import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestAudio
{
  final private static int FRAME_LENGTH = 3200;
  
  private static InputStream inputFileStream;
  private static long fileLength;
  
  private static void die(String s)
  {
    System.err.println(s);
    System.exit(1);
  }
  
  public static void main(String[] args)
  {
    try
    {
      File file = new File(args[0]);
      inputFileStream = new BufferedInputStream(new FileInputStream(file));
      fileLength = file.length();
      System.out.println(fileLength);
    }
    catch (FileNotFoundException e) {die("FileNotFound " + args[0]);}
    
    long offset = 0;
    int num = 0;
    long sum = 0;
    
    try
    {
      byte[] bytes = new byte[FRAME_LENGTH];
      inputFileStream.skip(44);
      //inputFileStream.skip(3200);
      //inputFileStream.skip(8640000);  // loud
      inputFileStream.skip(12672000);  // quiet
      //inputFileStream.read(bytes, (int)offset, FRAME_LENGTH);
/*
      for (int x = 0; x < 3200; x += 2)
      {
        System.out.println(bytes[x] + " " + bytes[x + 1]);
      }
*/
/*
      for (int i = 0; i < 60; i++)
      {
        for (int j = 0; j < 30; j++)
        {
          for (int k = 0; k < bytes.length; k += 2)
          {
            int num1 = int i = (((bytes[k] & 0xFF) << 8) | (bytes[k+1] & 0xFF));
            int num2 = (((bytes[k+1] & 0xFF) << 8) | (bytes[k] & 0xFF));
          }
        }
      }
*/
      double sum1 = 0, sum2 = 0;
      double rms1 = 0, rms2 = 0;
      for (int i = 0; i < 30; i++)
      {
        for (int j = 0; j < 1600; j++)
        {
          inputFileStream.read(bytes, (int)offset, FRAME_LENGTH);
          int num1 = (((bytes[j] & 0xFF) << 8) | (bytes[j+1] & 0xFF));
          int num2 = (((bytes[j+1] & 0xFF) << 8) | (bytes[j] & 0xFF));
          
          sum1 = Math.pow(num1, 2);
          sum2 = Math.pow(num2, 2);
          num++;
        }
        rms1 = Math.sqrt(sum1 / num);
        rms2 = Math.sqrt(sum2 / num);
        System.out.println("rms1 = " + rms1 + " | rms2 = " + rms2);
        sum1 = 0;
        sum2 = 0;
        rms1 = 0;
        rms2 = 0;
        num = 0;
      }
/*      
      for (int x = 0; x < bytes.length; x += 2)
      {
        int i = (((bytes[x] & 0xFF) << 8) | (bytes[x+1] & 0xFF));
        int j = (((bytes[x + 1] & 0xFF) << 8) | (bytes[x] & 0xFF));
        System.out.println(bytes[x] + " " + bytes[x + 1] + ":" + i + ":" + j);
        sum += Math.abs(i);
        num++;
      }
*/
/*
      for (byte b: bytes)
      {
        int i = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt() + 128;
        //System.out.println(i);
        sum += i;
        num++;
      }     
*/
/*
      System.out.println("num = " + num);
      System.out.println("sum = " + sum);
      System.out.println("average = " + sum / num);
*/
    }
    catch (IOException e) {die("IOException");}
  }
}