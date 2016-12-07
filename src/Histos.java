import java.awt.Color;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Histos
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
  final public static int NFL_FRAME_LOCATION = 2213;  // 1m13s
  final public static int STARBUCKS_FRAME_LOCATION = 5375; // 3m
  final public static int SUBWAY_FRAME_LOCATION = 1800;
  final public static int HEIGHT = 270;
  final public static int WIDTH = 480;
  final public static int PIXELS_PER_FRAME = 3 * HEIGHT * WIDTH;
  
  private static byte[] dataSB, dataSW, dataM, dataN;
  private static int[] confSB, confSW, confM, confN;
  private static int[] hHistSB, hHistSW, hHistM, hHistN;
  private static int pixelsSB, pixelsSW, pixelsM, pixelsN;
  
  
  public static double[] RGBtoHSL(int R, int G, int B){
		double[] hsl= new double[3];	
		float r = R / 255.f;
			 float g = G / 255.f;
			 float b = B/ 255.f;
			 float max = Math.max(Math.max(r, g), b);
			 float min = Math.min(Math.min(r, g), b);
			 float c = max - min;
			 
			 float h_ = 0.f;
			 if (c == 0) {
			  h_ = 0;
			 } else if (max == r) {
			  h_ = (float)(g-b) / c;
			  if (h_ < 0) h_ += 6.f;
			 } else if (max == g) {
			  h_ = (float)(b-r) / c + 2.f;
			 } else if (max == b) {
			  h_ = (float)(r-g) / c + 4.f;
			 }
			 float h = 60.f * h_;
			 
			 float l = (max + min) * 0.5f;
			 
			 float s;
			 if (c == 0) {
			  s = 0.f;
			 } else {
			  s = c / (1 - Math.abs(2.f * l - 1.f));
			 }
			 
			 hsl[0] = h;
			 hsl[1] = s;
			 hsl[2] = l;
		return hsl;
	}
  
  private static void closeFile(RandomAccessFile raf)
  {
    try
    {
      raf.close();
    }
    catch (IOException e) {Utilities.die("IOException");}
  }
  
  private static void computeConfidence(int[] frameHist)
  {
    double[] quoSB = new double[361];
    double[] quoSW = new double[361];
    double[] quoM = new double[361];
    double[] quoN = new double[361];
    
    confSB = new int[361];
    confSW = new int[361];
    confM = new int[361];
    confN = new int[361];
    
    double maxSB = 0;
    double maxSW = 0;
    double maxM = 0;
    double maxN = 0;
    
    for (int i = 0; i < frameHist.length; i++)
    {
      if (frameHist[i] == 0)
      {
        quoSB[i] = 0;
        quoSW[i] = 0;
        quoM[i] = 0;
        quoN[i] = 0;
      }
      else
      {
        if (hHistSB[i] == 0) quoSB[i] = 0;
        else
        {
          quoSB[i] = (double)hHistSB[i] / frameHist[i];
          if (quoSB[i] > maxSB) maxSB = quoSB[i];
        }

        if (hHistSW[i] == 0) quoSW[i] = 0;
        else
        {
          quoSW[i] = (double)hHistSW[i] / frameHist[i];
          if (quoSW[i] > maxSW) maxSW = quoSW[i];
        }

        if (hHistM[i] == 0) quoM[i] = 0;
        else
        {
          quoM[i] = (double)hHistM[i] / frameHist[i];
          if (quoM[i] > maxM) maxM = quoM[i];
        }

        if (hHistN[i] == 0) quoN[i] = 0;
        else
        {
          quoN[i] = (double)hHistN[i] / frameHist[i];
          if (quoN[i] > maxN) maxN = quoN[i];
        }
      }
    }
    
    for (int i = 0; i < quoSB.length; i++)
    {
      confSB[i] = (int)((quoSB[i] / maxSB) * 255);
      confSW[i] = (int)((quoSW[i] / maxSW) * 255);
      confM[i] = (int)((quoM[i] / maxM) * 255);
      confN[i] = (int)((quoN[i] / maxN) * 255);
    }
  }
  
  private static void computeNewConfidence(int[] frameHist)
  {
    double[] quoSB = new double[361];
    double[] quoSW = new double[361];
    double[] quoM = new double[361];
    double[] quoN = new double[361];
    
    confSB = new int[361];
    confSW = new int[361];
    confM = new int[361];
    confN = new int[361];
    
    int pixelsFrame = WIDTH * HEIGHT;
 
    
    for (int i = 0; i < frameHist.length; i++)
    {
      quoSB[i] = piShapeFuncValue(0, 0.25 * (double)hHistSB[i] / pixelsSB,
        0.75 * (double)hHistSB[i] / pixelsSB, (double)hHistSB[i] / pixelsSB,
        (double)frameHist[i] / pixelsFrame);
      quoSW[i] = piShapeFuncValue(0, 0.25 * (double)hHistSW[i] / pixelsSW,
        0.75 * (double)hHistSW[i] / pixelsSW, (double)hHistSW[i] / pixelsSW,
        (double)frameHist[i] / pixelsFrame);
      quoM[i] = piShapeFuncValue(0, 0.25 * (double)hHistM[i] / pixelsM,
        0.75 * (double)hHistM[i] / pixelsM, (double)hHistM[i] / pixelsM,
        (double)frameHist[i] / pixelsFrame);
      quoN[i] = piShapeFuncValue(0, 0.25 * (double)hHistN[i] / pixelsN,
        0.75 * (double)hHistN[i] / pixelsN, (double)hHistN[i] / pixelsN,
        (double)frameHist[i] / pixelsFrame);
System.out.println(i + ": " + ((double)hHistSB[i] / pixelsSB) + " " + ((double)hHistSW[i] / pixelsSW) + " " + ((double)hHistM[i] / pixelsM) + " " + ((double)hHistN[i] / pixelsN));
    }
    
    for (int i = 0; i < quoSB.length; i++)
    {
      System.out.println(i + ": " + quoSB[i] + " " + quoSW[i] + " " + quoM[i] + " " + quoN[i]);
    }
    
    for (int i = 0; i < quoSB.length; i++)
    {
      confSB[i] = (int)(quoSB[i] * 255);
      confSW[i] = (int)(quoSW[i] * 255);
      confM[i] = (int)(quoM[i] * 255);
      confN[i] = (int)(quoN[i] * 255);
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
  
  private static void displayLogoComparisons(BufferedImage bi,
    BufferedImage biSB, BufferedImage biSW, BufferedImage biM,
    BufferedImage biN)
  {
    JLabel jl = new JLabel(new ImageIcon(bi));
    JLabel jlSB = new JLabel(new ImageIcon(biSB));
    JLabel jlSW = new JLabel(new ImageIcon(biSW));
    JLabel jlM = new JLabel(new ImageIcon(biM));
    JLabel jlN = new JLabel(new ImageIcon(biN));
    
    JFrame jf = new JFrame();
    jf.setLayout(new GridLayout(3, 2, 5, 5));
    jf.getContentPane().add(jlSB);
    jf.getContentPane().add(jlSW);
    jf.getContentPane().add(jlM);
    jf.getContentPane().add(jlN);
    jf.getContentPane().add(jl);
    jf.pack();
    jf.setVisible(true);
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        int pixel = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) |
          (b & 0xff);
        bi.setRGB(x, y, pixel);
        index++;
      }
    }
  }
  
  private static byte[] getData(String inputFile, int offset)
  {
    RandomAccessFile raf = openFile(inputFile);
    byte[] b = read(raf, offset);
    closeFile(raf);
    
    return(b);
  }
  
  private static int[] getFocusedHHistogram(byte[] data)
  {
    int[] hHistogram = new int[361];
    
/* Comment out to see what was trimmed
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
Comment out to see what was trimmed */
    
    byte rAnchor = data[3];
    byte gAnchor = data[3 + (HEIGHT * WIDTH)];
    byte bAnchor = data[3 + (2 * HEIGHT * WIDTH)];
    
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
      while (offsetLeft < (WIDTH - offsetRight))
      {
        rLeft = data[(i * WIDTH) + offsetLeft];
        gLeft = data[(i * WIDTH) + offsetLeft + (HEIGHT * WIDTH)];
        bLeft = data[(i * WIDTH) + offsetLeft + (2 * HEIGHT * WIDTH)];
        
/* Comment out to see what was trimmed
        newData[(i * WIDTH) + offsetLeft] = rLeft;
        newData[(i * WIDTH) + offsetLeft + (HEIGHT * WIDTH)] = gLeft;
        newData[(i * WIDTH) + offsetLeft + (2 * HEIGHT * WIDTH)] = bLeft;
Comment out to see what was trimmed */
        /*
        float[] hsb = new float[3];
        Color.RGBtoHSB((int)rLeft + 128, (int)gLeft + 128, (int)bLeft + 128, hsb);
        int h = (int)(hsb[0] * 360);
        */
        double[] hsl = new double[3];
        RGBtoHSL((int)rLeft + 128, (int)gLeft + 128,
          (int)bLeft + 128);
        int h = (int)(hsl[0] * 360);
//        hHistogram[h]++;
//if (hsb[1] < 0.3 && hsb[2] > 0.7)
//{
//  System.out.println(hsb[0] + " " + hsb[1] + " " + hsb[2]);
//}
//if (hsb[2] > 0.6)
hHistogram[h]++;//else hHistogram[h]++;

//System.out.println(hsb[0] + " " + hsb[1] + " " + hsb[2]);
          
        offsetLeft++;
      }
    }
    //showImage(newData);  // Uncomment to see what was trimmed
    
    return(hHistogram);
  }
  
  private static int[] getHHistogram(byte[] data)
  {
    int[] hHistogram = new int[361];
    
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        byte r = data[index];
        byte g = data[index + (HEIGHT * WIDTH)];
        byte b = data[index + (2 * HEIGHT * WIDTH)];
        /*
        float[] hsb = new float[3];
        Color.RGBtoHSB((int)r + 128, (int)g + 128, (int)b + 128, hsb);
        int h = (int)(hsb[0] * 360);
        */
        
        double[] hsl = new double[3];
        RGBtoHSL((int)r + 128, (int)g + 128,
          (int)b + 128);
        int h = (int)(hsl[0] * 360);
        hHistogram[h]++;
        
        index++;
      }
    }
    
    return(hHistogram);
  }
  
  private static int[] getHOfHSLHistogram(byte[] data)
  {
    int[] hHistogram = new int[361];
    
    for (int i = 0; i < HEIGHT; i++)
    {
      for (int j = 0; j < WIDTH; j++)
      {
        byte r = data[(i * WIDTH) + j];
        byte g = data[(i * WIDTH) + j + (WIDTH * HEIGHT)];
        byte b = data[(i * WIDTH) + j + (2 * WIDTH * HEIGHT)];

        int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
        int Bl = (pix) & 0xff;
        int G = (pix >> 8) & 0xff;
        int R = (pix >> 16) & 0xff;

        double[] hsl = new double[3];
        hsl = RGBtoHSL(R, G, Bl);
        int h = (int)hsl[0];
        if (hsl[2] < 0.875) hHistogram[h]++;
      }
    }
    return(hHistogram);
  }
  
  private static int[] getMiddleHOfHSLHistogram(byte[] data)
  {
    int[] hHistogram = new int[361];

    byte[] newData = new byte[data.length];
    for (int i = 0; i < newData.length; i++)
    {
      newData[i] = 0;
    }
    for (int i = 12; i < (12 + 255); i++)
    {
      for (int j = 112; j < (112 + 255); j++)
      {
        newData[(i * WIDTH) + j] = data[(i * WIDTH) + j];
        newData[(i * WIDTH) + j + (WIDTH * HEIGHT)] =
          data[(i * WIDTH) + j + (WIDTH * HEIGHT)];
        newData[(i * WIDTH) + j + (2 * WIDTH * HEIGHT)] =
          data[(i * WIDTH) + j + (2 * WIDTH * HEIGHT)];
      }
    }
    showImage(newData);

    for (int i = 12; i < (12 + 256); i++)
    {
      for (int j = 112; j < (112 + 256); j++)
      {
        byte r = data[(i * WIDTH) + j];
        byte g = data[(i * WIDTH) + j + (WIDTH * HEIGHT)];
        byte b = data[(i * WIDTH) + j + (2 * WIDTH * HEIGHT)];
        
        int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
        int Bl = (pix) & 0xff;
        int G = (pix >> 8) & 0xff;
        int R = (pix >> 16) & 0xff;
        
        double[] hsl = new double[3];
        hsl = RGBtoHSL(R, G, Bl);
        //RGBtoHSL((int)(r & 0xff)+ 128, (int)(g & 0xff) + 128,
        //  (int)(b & 0xff) + 128);
System.out.println(R + " " + G + " " + Bl);
        int h = (int)hsl[0];
System.out.println(hsl[0] + " " + hsl[1] + " " + hsl[2]);
        if (hsl[2] < 0.875) hHistogram[h]++;
      }
    }
    
    return(hHistogram);
  }
  
  private static void initLogoData()
  {
    dataSB = getData(STARBUCKS_LOGO, 0);
    dataSW = getData(SUBWAY_LOGO, 0);
    dataM = getData(MCD_LOGO, 0);
    dataN = getData(NFL_LOGO, 0);
  }
  
  private static void initLogoHHistograms()
  {
    hHistSB = getFocusedHHistogram(dataSB);
    hHistSW = getFocusedHHistogram(dataSW);
    hHistM = getFocusedHHistogram(dataM);
    hHistN = getFocusedHHistogram(dataN);
    
    pixelsSB = 0;
    pixelsSW = 0;
    pixelsM = 0;
    pixelsN = 0;
    
    for (int i = 0; i < hHistSB.length; i++)
    {
      pixelsSB += hHistSB[i];
      pixelsSW += hHistSW[i];
      pixelsM += hHistM[i];
      pixelsN += hHistN[i];
    }
  }
  
  private static void initLogoMiddleHHistograms()
  {
    hHistSB = getMiddleHOfHSLHistogram(dataSB);
    hHistSW = getMiddleHOfHSLHistogram(dataSW);
    hHistM = getMiddleHOfHSLHistogram(dataM);
    hHistN = getMiddleHOfHSLHistogram(dataN);
  }
  
  private static RandomAccessFile openFile(String inputFile)
  {
    try
    {
      RandomAccessFile raf = new RandomAccessFile(inputFile, "r");
      return(raf);
    }
    catch (IOException e) {Utilities.die("IOException");}
    return(null);
  }
  
  private static double piShapeFuncValue(double a, double b, double c,
    double d, double x)
  {
    if (x <= a) return 0;
    else if (a <= x && x <= ((a + b) / 2))
      return(2 * Math.pow(((x - a) / (b - a)), 2));
    else if (((a + b) / 2) <= x && x <= b)
      return(1 - Math.pow((x - b) / (b - a), 2));
    else if (b <= x && x <= c)
      return 1;
    else if (c <= x && x <= ((c + d) / 2))
      return(1 - Math.pow((x - c) / (d - c), 2));
    else if (((c + d) / 2) <= x && x <= d)
      return(2 * Math.pow(((x - d) / (d - c)), 2));
    else if (x >= d)
      return 0;
    return 0;
  }
  
  private static void printLogoHistograms()
  {
    for (int i = 0; i < hHistSB.length; i++)
    {
      System.out.println(i + ": " + hHistSB[i] + " " + hHistSW[i] + " " +
        hHistM[i] + " " + hHistN[i]);
    }
  }
  
  private static byte[] read(RandomAccessFile raf, int offset)
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
  
  private static void showImage(byte[] data)
  {
    BufferedImage bi = new BufferedImage(WIDTH, HEIGHT,
      BufferedImage.TYPE_INT_RGB);
    drawImage(data, bi);
    display(bi);
  }
  
  
  public static void main(String[] args)
  {
    initLogoData();
    //initLogoHHistograms();
    initLogoMiddleHHistograms();

    String inputFileName = INPUT_FILE_1;
    if (Integer.parseInt(args[0]) == 2) inputFileName = INPUT_FILE_2;
    int frameNum = Integer.parseInt(args[1]);
    
    byte[] data = getData(inputFileName, frameNum);
    int[] frameHHist = getHOfHSLHistogram(data);
    //computeConfidence(frameHHist);
    computeNewConfidence(frameHHist);
    
    BufferedImage bi = new BufferedImage(WIDTH, HEIGHT,
      BufferedImage.TYPE_INT_RGB);
    BufferedImage biSB = new BufferedImage(WIDTH, HEIGHT,
      BufferedImage.TYPE_INT_RGB);
    BufferedImage biSW = new BufferedImage(WIDTH, HEIGHT,
      BufferedImage.TYPE_INT_RGB);
    BufferedImage biM = new BufferedImage(WIDTH, HEIGHT,
      BufferedImage.TYPE_INT_RGB);
    BufferedImage biN = new BufferedImage(WIDTH, HEIGHT,
      BufferedImage.TYPE_INT_RGB);
    
    byte[] confDataSB = new byte[data.length];
    byte[] confDataSW = new byte[data.length];
    byte[] confDataM = new byte[data.length];
    byte[] confDataN = new byte[data.length];
    
    int index = 0;
    for (int y = 0; y < HEIGHT; y++)
    {
      for (int x = 0; x < WIDTH; x++)
      {
        byte r = data[index];
        byte g = data[index + (HEIGHT * WIDTH)];
        byte b = data[index + (2 * HEIGHT * WIDTH)];

        /*
        float[] hsb = new float[3];
        Color.RGBtoHSB((int)r + 128, (int)g + 128, (int)b + 128, hsb);
        int h = (int)(hsb[0] * 360);
        */
        
        /*
        double[] hsl = new double[3];
        RGBtoHSL((int)r + 128, (int)g + 128,
          (int)b + 128);
        int h = (int)(hsl[0] * 360);
        */
        int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
        int Bl = (pix) & 0xff;
        int G = (pix >> 8) & 0xff;
        int R = (pix >> 16) & 0xff;

        double[] hsl = new double[3];
        hsl = RGBtoHSL(R, G, Bl);
        int h = (int)hsl[0];
        
        confDataSB[index] = (byte)(confSB[h] - 128);
        confDataSB[index + (HEIGHT * WIDTH)] = (byte)(confSB[h] - 128);
        confDataSB[index + (2 * HEIGHT * WIDTH)] = (byte)(confSB[h] - 128);
        
        confDataSW[index] = (byte)(confSW[h] - 128);
        confDataSW[index + (HEIGHT * WIDTH)] = (byte)(confSW[h] - 128);
        confDataSW[index + (2 * HEIGHT * WIDTH)] = (byte)(confSW[h] - 128);
        
        confDataM[index] = (byte)(confM[h] - 128);
        confDataM[index + (HEIGHT * WIDTH)] = (byte)(confM[h] - 128);
        confDataM[index + (2 * HEIGHT * WIDTH)] = (byte)(confM[h] - 128);
        
        confDataN[index] = (byte)(confN[h] - 128);
        confDataN[index + (HEIGHT * WIDTH)] = (byte)(confN[h] - 128);
        confDataN[index + (2 * HEIGHT * WIDTH)] = (byte)(confN[h] - 128);

        index++;
      }
    }
/*
for (int i = 0; i < confDataSB.length; i++)
{
  System.out.println(i + ": " + confDataSB[i] + " " + confDataSW[i] + " " + confDataM[i] + " " + confDataN[i]);
}
*/
    
    drawImage(data, bi);
    drawImage(confDataSB, biSB);
    drawImage(confDataSW, biSW);
    drawImage(confDataM, biM);
    drawImage(confDataN, biN);
    
    displayLogoComparisons(bi, biSB, biSW, biM, biN);
  }
}