import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ScanAndBox
{
  final public static String INPUT_FILE_1 =
    "../../dataset/Videos/data_test1.rgb";
  final public static String INPUT_FILE_2 =
    "../../dataset2/Videos/data_test2.rgb";
  final public static String SB_INPUT_FILE = "sb.rgb";
  final public static String OUTPUT_FILE = "new_data_test1.rgb";
  final public static String STARBUCKS_LOGO =
    "../../dataset/Brand Images/starbucks_logo.rgb";
  final public static String SUBWAY_LOGO =
    "../../dataset/Brand Images/subway_logo.rgb";
  final public static String MCD_LOGO =
    "../../dataset2/Brand Images/Mcdonalds_logo.raw";
  final public static String NFL_LOGO =
    "../../dataset2/Brand Images/nfl_logo.rgb";
  final public static int MCD_FRAME_LOCATION = 4390;
  final public static int NFL_FRAME_LOCATION = 2212;  // 1m13s
  final public static int STARBUCKS_FRAME_LOCATION = 5375; // 3m
  final public static int SUBWAY_FRAME_LOCATION = 1800;
  final public static int HEIGHT = 270;
  final public static int WIDTH = 480;
  final public static int PIXELS_PER_FRAME = 3 * HEIGHT * WIDTH;
  
  private static Map<Integer, Integer> hmHLogo;
  private static Map<Float, Integer> hm;
  private static Map<Double, Integer> hmFrame;
  private static Map<Double, Integer> hmLogo;
  private static Map<Double, Integer> hmQuotient;
  private static RandomAccessFile raf;
  private static int[] histFrame;
  private static int[] histFrameA;
  private static int[] histFrameB;
  private static int[] histLogo;
  private static int[] histLogoA;
  private static int[] histLogoB;
  private static double[] quotient;
  private static double[] quotientA;
  private static double[] quotientB;
  private static long fileLength;
  
  private static ArrayList<LabColor> labLogo;
  
  private static void closeFile()
  {
    try
    {
      raf.close();
    }
    catch (IOException e) {Utilities.die("IOException");}
  }
  
  private static double deltaE94(LabColor lc1, LabColor lc2)
  {
    double weightL = 1;
    double weightC = 1;
    double weightH = 1;
    double xC1 = Math.sqrt((lc1.a * lc1.a) + (lc1.b * lc1.b));
    double xC2 = Math.sqrt((lc2.a * lc2.a) + (lc2.b * lc2.b));
    double xDL = lc2.L - lc1.L;
    double xDC = xC2 - xC1;
    double xDE = Math.sqrt(((lc1.L - lc2.L) * (lc1.L - lc2.L)) +
      ((lc1.a - lc2.a) * (lc1.a - lc2.a)) +
      ((lc1.b - lc2.b) * (lc1.b - lc2.b)));
    double xDH = 0;
    if (Math.sqrt(xDE) > (Math.sqrt(Math.abs(xDL)) +
      Math.sqrt(Math.abs(xDC))))
    {
      xDH = Math.sqrt((xDE * xDE) - (xDL * xDL) - (xDC * xDC));
    }
    double xSC = 1 + (0.045 * xC1);
    double xSH = 1 + (0.015 * xC1);
    xDL /= weightL;
    xDC /= weightC * xSC;
    xDH /= weightH * xSH;
    
    return(Math.sqrt((xDL * xDL) + (xDC * xDC) + (xDH * xDH)));
  }
  
  private static void drawImage(byte[] data, BufferedImage bi)
  {
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        byte r = data[index];
        byte g = data[index + (HEIGHT * WIDTH)];
        byte b = data[index + (2 * HEIGHT * WIDTH)];
/*        
int leftCorner = 120 + (68 * WIDTH);
for (int i = 0; i < 68; i++)
{
  if (index > (leftCorner + (i * WIDTH)) && index < (leftCorner + 120 +
    (i * WIDTH)))
  {
    r = 0; g = 0; b = 0;
    break;
  }
}
*/
        int pixel = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) |
          (b & 0xff);
        bi.setRGB(x, y, pixel);
        index++;
      }
    }
  }
  
  private static void display(BufferedImage bi)
  {
    JFrame jf = new JFrame();
    JLabel jl = new JLabel(new ImageIcon(bi));
    jf.getContentPane().add(jl);
    jf.pack();
    jf.setVisible(true);
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
  
  private static void getFrameLabHistogram()
  {
    openFile(SB_INPUT_FILE);
    byte[] data = read(0);
    closeFile();
/*    
  hmFrame = new HashMap<Double, Integer>();
*/
    histFrame = new int[101];
    histFrameA = new int[255];
    histFrameB = new int[255];
    
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        byte r = data[index];
        byte g = data[index + (HEIGHT * WIDTH)];
        byte b = data[index + (2 * HEIGHT * WIDTH)];
        
        double[] labValues = ColorSpaceConverter.RGBtoLAB(r & 0xff, g & 0xff,
          b & 0xff);
        int l = (int)labValues[0];
        int a = (int)labValues[1] + 128;
        int bOfLab = (int)labValues[2] + 128;
        histFrame[l]++;
        histFrameA[a]++;
        histFrameB[bOfLab]++;
/*
        double l = labValues[0];
        if (hmFrame.containsKey(l))
        {
          hmFrame.put(l, new Integer((hmFrame.get(l)).intValue() + 1));
        }
        else
        {
          hmFrame.put(l, new Integer(1));
        }
*/
        
        index++;
      }
    }
/*
for (int i = 0; i < 255; i++)
{
  System.out.print(i + ":");
  if (i < 101)
  {
    System.out.println(histFrame[i] + " " + histFrameA[i] + " " +
      histFrameB[i]);
  }
  else
  {
    System.out.println(histFrameA[i] + " " + histFrameB[i]);
  }
}
*/
/*
    Iterator it = hmFrame.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry pair = (Map.Entry)it.next();
      System.out.println(pair.getKey() + " " + pair.getValue());
      it.remove();
    }
*/
  }
  
  private static void getLogoHSV()
  {
    hm = new HashMap<Float, Integer>();
    openFile(STARBUCKS_LOGO);
    byte[] data = read(0);
    closeFile();
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        byte r = data[index];
        byte g = data[index + (HEIGHT * WIDTH)];
        byte b = data[index + (2 * HEIGHT * WIDTH)];
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        float h = hsb[0] * 360;
        if (hm.containsKey(h))
        {
          hm.put(h, new Integer((hm.get(h)).intValue() + 1));
        }
        else
        {
          hm.put(h, new Integer(1));
        }
        index++;
      }
    }
    Iterator it = hm.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry pair = (Map.Entry)it.next();
      System.out.println(pair.getKey() + " " + pair.getValue());
      it.remove();
    }
  }
  
  private static void getLogoLabHistogram()
  {
    openFile(SUBWAY_LOGO);
    byte[] data = read(0);
    closeFile();
    
    hmLogo = new HashMap<Double, Integer>();
    histLogo = new int[101];
    histLogoA = new int[255];
    histLogoB = new int[255];
    labLogo = new ArrayList<LabColor>();
    
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        byte r = data[index];
        byte g = data[index + (HEIGHT * WIDTH)];
        byte b = data[index + (2 * HEIGHT * WIDTH)];
        
        double[] labValues = ColorSpaceConverter.RGBtoLAB(r & 0xff, g & 0xff,
          b & 0xff);
        int l = (int)labValues[0];
        int a = (int)labValues[1] + 128;
        int bOfLab = (int)labValues[2] + 128;
        labLogo.add(new LabColor(l, a, bOfLab));
        histLogo[l]++;
        histLogoA[a]++;
        histLogoB[bOfLab]++;
/*
        double l = labValues[0];
        if (hmLogo.containsKey(l))
        {
          hmLogo.put(l, new Integer((hmLogo.get(l)).intValue() + 1));
        }
        else
        {
          hmLogo.put(l, new Integer(1));
        }
*/
        
        index++;
      }
    }
/*
    Iterator it = hmLogo.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry pair = (Map.Entry)it.next();
      System.out.println(pair.getKey() + " " + pair.getValue());
      it.remove();
    }
*/
  }
  
  private static void getLogoLabHistogram(String logo)
  {
    openFile(logo);
    byte[] data = read(0);
    closeFile();
    
    hmLogo = new HashMap<Double, Integer>();
    histLogo = new int[101];
    histLogoA = new int[255];
    histLogoB = new int[255];
    labLogo = new ArrayList<LabColor>();
    
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        byte r = data[index];
        byte g = data[index + (HEIGHT * WIDTH)];
        byte b = data[index + (2 * HEIGHT * WIDTH)];
        
        double[] labValues = ColorSpaceConverter.RGBtoLAB(r & 0xff, g & 0xff,
          b & 0xff);
        int l = (int)labValues[0];
        int a = (int)labValues[1] + 128;
        int bOfLab = (int)labValues[2] + 128;
        labLogo.add(new LabColor(l, a, bOfLab));
        histLogo[l]++;
        histLogoA[a]++;
        histLogoB[bOfLab]++;
        
        index++;
      }
    }
  }
  
  private static void getLogoHHistogram(String logo)
  {
    openFile(logo);
    byte[] data = read(0);
    closeFile();
    
    //hmHLogo = new HashMap<Integer, Integer>();
    histLogo = new int[361];
    
/*
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        byte r = data[index];
        byte g = data[index + (HEIGHT * WIDTH)];
        byte b = data[index + (2 * HEIGHT * WIDTH)];
        
        float[] hsb = new float[3];
        Color.RGBtoHSB(r, g, b, hsb);
        int h = (int)(hsb[0] * 360);
        //int h = (int)(Color.RGBtoHSB((int)r, (int)g, (int)b, null)[0]);
//System.out.println(h);
        
        histLogo[h]++;
        
        index++;
      }
    }
*/
    
    byte[] newData = new byte[data.length];
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        newData[index] = (byte)255;
        newData[index + (HEIGHT * WIDTH)] = (byte)255;
        newData[index + (2 * HEIGHT * WIDTH)] = (byte)0;
        
        index++;
      }
    }
    
    int counter = 0;
    byte rAnchor = data[3];
    byte gAnchor = data[3 + (HEIGHT * WIDTH)];
    byte bAnchor = data[3 + (2 * HEIGHT * WIDTH)];
/*
    byte rAnchorLeft = data[3];
    byte gAnchorLeft = data[3 + (HEIGHT * WIDTH)];
    byte bAnchorLeft = data[3 + (2 * HEIGHT * WIDTH)];
    byte rAnchorRight = data[3];
    byte gAnchorRight = gRight;
    byte bAnchorRight = bRight;
*/
    
    for (int i = 0; i < HEIGHT; i++)
    {
      int offsetLeft = 3;
      int offsetRight = 3;
      byte rLeft = data[(i * WIDTH) + offsetLeft];
      byte gLeft = data[(i * WIDTH) + offsetLeft + (HEIGHT * WIDTH)];
      byte bLeft = data[(i * WIDTH) + offsetLeft + (2 * HEIGHT * WIDTH)];
      byte rRight = data[(i * WIDTH) + WIDTH - offsetRight];
      byte gRight = data[(i * WIDTH) + WIDTH - offsetRight +
        (HEIGHT * WIDTH)];
      byte bRight = data[(i * WIDTH) + WIDTH  - offsetRight + (2 * HEIGHT *
        WIDTH)];
      
      
      while (offsetLeft < WIDTH / 2)
      {
        rLeft = data[(i * WIDTH) + offsetLeft];
        gLeft = data[(i * WIDTH) + offsetLeft + (HEIGHT * WIDTH)];
        bLeft = data[(i * WIDTH) + offsetLeft + (2 * HEIGHT * WIDTH)];
        
        if (rLeft != rAnchor && gLeft != gAnchor && bLeft != bAnchor)
        {
          break;
        }
        
        offsetLeft++;
      }
      while (offsetRight < WIDTH / 2)
      {
        rRight = data[(i * WIDTH) + WIDTH - offsetRight];
        gRight = data[(i * WIDTH) + WIDTH - offsetRight + (HEIGHT * WIDTH)];
        bRight = data[(i * WIDTH) + WIDTH - offsetRight + (2 * HEIGHT *
          WIDTH)];
        
        if (rRight != rAnchor && gRight != gAnchor && bRight != bAnchor)
        {
          break;
        }
        
        offsetRight++;
      }
      if (offsetLeft == WIDTH / 2 && offsetLeft != offsetRight)
      {
        int end = WIDTH - offsetRight;
        for (int j = 0; j < end; j++)
        {
          rLeft = data[(i * WIDTH) + offsetLeft];
          gLeft = data[(i * WIDTH) + offsetLeft + (HEIGHT * WIDTH)];
          bLeft = data[(i * WIDTH) + offsetLeft + (2 * HEIGHT * WIDTH)];
        
          if (rLeft != rAnchor && gLeft != gAnchor && bLeft != bAnchor)
          {
            break;
          }
        
          offsetLeft++;
        }
      }
      else if (offsetRight == WIDTH / 2 && offsetRight != offsetLeft)
      {
        int end = (WIDTH / 2) - offsetLeft;
        for (int j = 0; j < end; j++)
        {
          rRight = data[(i * WIDTH) + WIDTH - offsetRight];
          gRight = data[(i * WIDTH) + WIDTH - offsetRight + (HEIGHT *
            WIDTH)];
          bRight = data[(i * WIDTH) + WIDTH - offsetRight + (2 * HEIGHT *
            WIDTH)];
        
        if (rRight != rAnchor && gRight != gAnchor && bRight != bAnchor)
        {
          break;
        }
        
          offsetRight++;
        }
      }
      //if (offsetLeft < (WIDTH / 2))
      //{
        while (offsetLeft < (WIDTH - offsetRight))
        {
          rLeft = data[(i * WIDTH) + offsetLeft];
          gLeft = data[(i * WIDTH) + offsetLeft + (HEIGHT * WIDTH)];
          bLeft = data[(i * WIDTH) + offsetLeft + (2 * HEIGHT * WIDTH)];
/*
newData[(i * WIDTH) + offsetLeft] = rLeft;
newData[(i * WIDTH) + offsetLeft + (HEIGHT * WIDTH)] = gLeft;
newData[(i * WIDTH) + offsetLeft + (2 * HEIGHT * WIDTH)] = bLeft;
*/
          float[] hsb = new float[3];
          Color.RGBtoHSB((int)rLeft + 128, (int)gLeft + 128, (int)bLeft + 128, hsb);
          //Color.RGBtoHSB(rLeft & 0xff, gLeft & 0xff, bLeft & 0xff, hsb);
          int h = (int)(hsb[0] * 360);
//System.out.println(h + " " + (rLeft & 0xff) + " " + (gLeft & 0xff) + " " + (bLeft & 0xff));
          histLogo[h]++;

          counter++;
          
          offsetLeft++;
        }
      //}
    }
System.out.println("counter: " + counter);
//showImage(newData);
  }
  
  private static void getQuotientHistogram()
  {
    quotient = new double[101];
    quotientA = new double[255];
    quotientB = new double[255];
    for (int i = 0; i < quotient.length; i++)
    {
      if (histFrame[i] == 0)
      {
        quotient[i] = 0;
      }
      else
      {
        quotient[i] = (double)histLogo[i] / histFrame[i];
      }
      
      if (histFrameA[i] == 0)
      {
        quotientA[i] = 0;
      }
      else
      {
        quotientA[i] = (double)histLogoA[i] / histFrameA[i];
      }
      
      if (histFrameB[i] == 0)
      {
        quotientB[i] = 0;
      }
      else
      {
        quotientB[i] = (double)histLogoB[i] / histFrameB[i];
      }
    }
    for (int i = 101; i < quotientA.length; i++)
    {
      if (histFrameA[i] == 0)
      {
        quotientA[i] = 0;
      }
      else
      {
        quotientA[i] = (double)histLogoA[i] / histFrameA[i];
      }
      
      if (histFrameB[i] == 0)
      {
        quotientB[i] = 0;
      }
      else
      {
        quotientB[i] = (double)histLogoB[i] / histFrameB[i];
      }
    }
/*    
    for (int i = 0; i < quotient.length; i++)
    {
      System.out.println(i + ":" + quotient[i] + "/" + histLogo[i] + " " +
        quotientA[i] + "/" + histLogoA[i] + " " + quotientB[i] +
        "/" + histLogoB[i]);
    }
    for (int i = 101; i < quotientA.length; i++)
    {
      System.out.println(i + ":" + quotientA[i] + "/" + histLogoA[i] + " " +
        quotientB[i] + "/" + histLogoB[i]);
    }
*/
  }
  
  private static byte[] read(int offset)
  {
    byte[] b = new byte[PIXELS_PER_FRAME];
    try
    {
      raf.skipBytes(offset * PIXELS_PER_FRAME);
      raf.read(b);
    }
    catch (FileNotFoundException e) {Utilities.die("FileNotFoundException");}
    catch (IOException e) {Utilities.die("IOException");}
    
    return(b);
  }
  
  private static void openFile(String inputFile)
  {
    try
    {
      raf = new RandomAccessFile(inputFile, "r");
      fileLength = raf.length();
    }
    catch (IOException e) {Utilities.die("IOException");}
  }
  
  private static void printHSV(byte[] data)
  {
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        byte r = data[index];
        byte g = data[index + (HEIGHT * WIDTH)];
        byte b = data[index + (2 * HEIGHT * WIDTH)];
        //int pixel = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) |
        //  (b & 0xff);
        
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        System.out.println((r + 128) + ":" + (g + 128) + ":" +
          (b + 128) + "|" + (hsb[0] * 360) + " " + hsb[1] + " " + hsb[2]);
        
        index++;
      }
    }
  }
  
  private static void printHSVOfSBLogo()
  {
    openFile(STARBUCKS_LOGO);
    byte[] frame = read(0);
    printHSV(frame);
    closeFile();
  }
  
  private static void saveQuotient()
  {
    openFile(SB_INPUT_FILE);
    byte[] data = read(0);
    closeFile();
    
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        byte r = data[index];
        byte g = data[index + (HEIGHT * WIDTH)];
        byte b = data[index + (2 * HEIGHT * WIDTH)];
        
        double[] labValues = ColorSpaceConverter.RGBtoLAB(r & 0xff, g & 0xff,
          b & 0xff);
        int l = (int)labValues[0];
        int a = (int)labValues[1] + 128;
        int bOfLab = (int)labValues[2] + 128;
/*
        if (quotient[l] != 0 && quotientA[a] != 0 && quotientB[bOfLab] != 0)
        {
          data[index] = (byte)255;
          data[index + (HEIGHT * WIDTH)] = (byte)0;
          data[index + (2 * HEIGHT * WIDTH)] = (byte)0;
        }
*/
        for (LabColor lc: labLogo)
        {
          if (l == lc.L)
          //if (l > (lc.L - 1) && l < (lc.L + 1))
          {
            if (a == lc.a)
            //if (a > (lc.a - 3) && a < (lc.a + 3))
            {
              if (bOfLab == lc.b)
              //if (bOfLab > (lc.b - 3) && bOfLab < (lc.b + 3))
              {
                data[index] = (byte)255;
                data[index + (HEIGHT * WIDTH)] = (byte)255;
                data[index + (2 * HEIGHT * WIDTH)] = (byte)255;
                break;
              }
            }
          }
          data[index] = (byte)0;
          data[index + (HEIGHT * WIDTH)] = (byte)0;
          data[index + (2 * HEIGHT * WIDTH)] = (byte)0;
        }
        
        if (index % 90 == 0)
        {
          data[index] = (byte)255;
          data[index + (HEIGHT * WIDTH)] = (byte)0;
          data[index + (2 * HEIGHT * WIDTH)] = (byte)0;
        }
        else if (index > (90 * WIDTH) && index < ((90 * WIDTH) + WIDTH))
        {
          data[index] = (byte)255;
          data[index + (HEIGHT * WIDTH)] = (byte)0;
          data[index + (2 * HEIGHT * WIDTH)] = (byte)0;
        }
        else if (index > (90 * WIDTH * 2) && index < ((90 * WIDTH * 2) + WIDTH))
        {
          data[index] = (byte)255;
          data[index + (HEIGHT * WIDTH)] = (byte)0;
          data[index + (2 * HEIGHT * WIDTH)] = (byte)0;
        }
        
        index++;
      }
    }
    
    showImage(data);
  }
  
  private static void showImage(byte[] data)
  {
    BufferedImage bi = new BufferedImage(WIDTH, HEIGHT,
      BufferedImage.TYPE_INT_RGB);
    drawImage(data, bi);
    display(bi);
  }
  
  private static void showSB()
  {
    byte[] frame = read(0);
    BufferedImage bi = new BufferedImage(WIDTH, HEIGHT,
      BufferedImage.TYPE_INT_RGB);
    drawImage(frame, bi);
    display(bi);
  }
  
  private static float toNewRange(byte b)
  {
    final float NEW_MAX = 255f;
    final float NEW_MIN = 0f;
    final float OLD_MAX = 127f;
    final float OLD_MIN = -128f;
    
    float newRange = NEW_MAX - NEW_MIN;
    float oldRange = OLD_MAX - OLD_MIN;
    
    return((((b - OLD_MIN) * newRange) / oldRange) + NEW_MIN);
  }
  
  private static void writeToDisk(byte[] data)
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(new File(OUTPUT_FILE));
      fos.write(data);
      fos.close();
    }
    catch (IOException e) {Utilities.die("IOException");}
  }
  
  public static void main(String[] args)
  {
    //openFile(SB_INPUT_FILE);
    //showSB();
    //closeFile();
    //getLogoLabHistogram();
    //getFrameLabHistogram();
    //getQuotientHistogram();
    //saveQuotient();
    //printHSVOfSBLogo();
    
    String inputFileName = INPUT_FILE_1;
    if (Integer.parseInt(args[0]) == 2) inputFileName = INPUT_FILE_2;
    int frameNum = Integer.parseInt(args[1]);
    String logo = "";
    if (args[2].equals("sb"))
    {
      logo = STARBUCKS_LOGO;
    }
    else if (args[2].equals("sw"))
    {
      logo = SUBWAY_LOGO;
    }
    else if (args[2].equals("m"))
    {
      logo = MCD_LOGO;
    }
    else if (args[2].equals("n"))
    {
      logo = NFL_LOGO;
    }
    displayLab(inputFileName, frameNum, logo);
  }
  
  private static void displayLab(String inputFileName, int frameNum,
    String logo)
  {
    openFile(inputFileName);
    byte[] data = read(frameNum);
    closeFile();    
    
    //getLogoLabHistogram(logo);
    getLogoHHistogram(logo);
/*
histLogo = new int[histLogo.length];
for (int k = 30; k < 81; k++)
{
  histLogo[k] = 1;
}
*/
    
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        byte r = data[index];
        byte g = data[index + (HEIGHT * WIDTH)];
        byte b = data[index + (2 * HEIGHT * WIDTH)];
/*
        int pixelHue = (int)(Color.RGBtoHSB(r & 0xff, g & 0xff, b & 0xff,
          null)[0] * 360);
*/        
        int pixelHue = (int)(Color.RGBtoHSB((int)r + 128, (int)g + 128, (int)b + 128, null)[0] * 360);
        
        for (int i = 0; i < histLogo.length; i++)
        {
          if (histLogo[i] > 0)
          {
            //if (pixelHue == i)
            if (pixelHue > (i - 5) && pixelHue < (i + 5))
            {
if (pixelHue > 80 && pixelHue < 180) // green is purple
{
  data[index] = (byte)255;
  data[index + (HEIGHT * WIDTH)] = (byte)0;
  data[index + (2 * HEIGHT * WIDTH)] = (byte)255;
}
else if (pixelHue > 180 && pixelHue < 270) // blue is light blue
{
  data[index] = (byte)0;
  data[index + (HEIGHT * WIDTH)] = (byte)255;
  data[index + (2 * HEIGHT * WIDTH)] = (byte)255;
}
else if (pixelHue > 0 && pixelHue < 80) // red is brown
{
  data[index] = (byte)128;
  data[index + (HEIGHT * WIDTH)] = (byte)64;
  data[index + (2 * HEIGHT * WIDTH)] = (byte)32;
}
else  // everything else is yellow
{
  data[index] = (byte)255;
  data[index + (HEIGHT * WIDTH)] = (byte)255;
  data[index + (2 * HEIGHT * WIDTH)] = (byte)0;
}
/*
              data[index] = (byte)255;
              data[index + (HEIGHT * WIDTH)] = (byte)0;
              data[index + (2 * HEIGHT * WIDTH)] = (byte)255;
*/
              break;
            }
          }
          else
          {
            data[index] = (byte)0;
            data[index + (HEIGHT * WIDTH)] = (byte)0;
            data[index + (2 * HEIGHT * WIDTH)] = (byte)0;
          }
        }
        
/*      
        double[] labValues = ColorSpaceConverter.RGBtoLAB(r & 0xff, g & 0xff,
          b & 0xff);
        int l = (int)labValues[0];
        int a = (int)labValues[1] + 128;
        int bOfLab = (int)labValues[2] + 128;

        for (LabColor lc: labLogo)
        {
          if (l == lc.L)
          //if (l > (lc.L - 1) && l < (lc.L + 1))
          {
            if (a == lc.a)
            //if (a > (lc.a - 3) && a < (lc.a + 3))
            {
              if (bOfLab == lc.b)
              //if (bOfLab > (lc.b - 3) && bOfLab < (lc.b + 3))
              {
                data[index] = (byte)255;
                data[index + (HEIGHT * WIDTH)] = (byte)255;
                data[index + (2 * HEIGHT * WIDTH)] = (byte)255;
                break;
              }
            }
          }
          data[index] = (byte)0;
          data[index + (HEIGHT * WIDTH)] = (byte)0;
          data[index + (2 * HEIGHT * WIDTH)] = (byte)0;
        }
*/
        
        if (index % 90 == 0)
        {
          data[index] = (byte)255;
          data[index + (HEIGHT * WIDTH)] = (byte)0;
          data[index + (2 * HEIGHT * WIDTH)] = (byte)0;
        }
        else if (index > (90 * WIDTH) && index < ((90 * WIDTH) + WIDTH))
        {
          data[index] = (byte)255;
          data[index + (HEIGHT * WIDTH)] = (byte)0;
          data[index + (2 * HEIGHT * WIDTH)] = (byte)0;
        }
        else if (index > (90 * WIDTH * 2) && index < ((90 * WIDTH * 2) + WIDTH))
        {
          data[index] = (byte)255;
          data[index + (HEIGHT * WIDTH)] = (byte)0;
          data[index + (2 * HEIGHT * WIDTH)] = (byte)0;
        }
        
        index++;
      }
    }
    
    showImage(data);
  }
  
  static class LabColor
  {
    public int L;
    public int a;
    public int b;
    
    LabColor(){}
    
    LabColor(int l, int A, int B)
    {
      this.L = l;
      this.a = A;
      this.b = B;
    }
  }
}