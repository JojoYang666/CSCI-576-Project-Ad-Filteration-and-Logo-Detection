import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class AudioCut {
	final private static String INPUT_FILE = "../../dataset/Videos/data_test1.wav";
	final private static int BYTES_PER_VIDEO_FRAME = 388800;
	final private static int HEADER_SIZE = 44;
	final private static int SAMPLES_PER_FRAME = 1600;

	private static ArrayList<Shot> shots;
	private static DataInputStream dis;
	private static double[] rmses;
	private static int bytesPerFrame = 2 * SAMPLES_PER_FRAME;

	private static void bucketSound() {
		try {
			dis.skip(44);

			long[] count = new long[Short.MAX_VALUE - Short.MIN_VALUE];
			double[] entropies = new double[shots.size()];

			for (int i = 0; i < shots.size(); i++) {
				long numAmps = (shots.get(i).getLengthOfShot() / BYTES_PER_VIDEO_FRAME) * SAMPLES_PER_FRAME;
				if (numAmps > (dis.available() / 2)) {
					numAmps = dis.available() / 2;
				}
				for (int j = 0; j < numAmps; j++) {
					dis.readShort();
				}
			}
		} catch (IOException e) {
			Utilities.die("IOException");
		}
	}

	private static void checkForZeroAmps() {
		try {
			dis.skip(44);

			ArrayList<Long> locations = new ArrayList<Long>();
			int count = 0;
			boolean isZero = false;
			long i = 0;

			while (dis.available() != 0) {
				int amp = dis.readShort();
				if (amp == 0) {
					count++;
					if (count == 2) {
						locations.add(i - (count - 1));
					}
				} else {
					count = 0;
				}
				i++;
			}

			for (int j = 0; j < locations.size(); j++) {
				locations.set(j, locations.get(j) / 1600);
			}

			for (long l : locations) {
				System.out.println(l);
			}
		} catch (IOException e) {
			Utilities.die("IOException");
		}
	}

	private static void findOutliers() {
		double sumRMS = 0;
		for (double rms : rmses) {
			sumRMS += rms;
		}
		double averageRMS = sumRMS / rmses.length;
		double sumRMSDifference = 0;
		for (double rms : rmses) {
			sumRMSDifference += Math.abs(rms - averageRMS);
		}
		double max = averageRMS + (4 * (sumRMSDifference / rmses.length));
		double min = averageRMS - (4 * (sumRMSDifference / rmses.length));
		System.out.println("Max: " + max + " Min: " + min);
		for (int i = 0; i < rmses.length; i++) {
			if (rmses[i] > max) {
				System.out.println("RMS" + i + " is out of threshold");
				// System.out.println("max: " + rmses[i] + " " + max);
			} else if (rmses[i] < min) {
				System.out.println("RMS" + i + " is out of threshold");
				// System.out.println("min: " + rmses[i] + " " + min);
			}
		}
	}

	private static void findOutliers2() {
		for (Shot shot : shots) {
			long start = shot.getStartingByte();
			long length = shot.getLengthOfShot();
			int framesInShot = (int) (length / BYTES_PER_VIDEO_FRAME);
			double sum = 0;
			for (int i = 0; i < framesInShot; i++) {
				sum += rmses[i];
			}
			shot.setRMSMean(sum / framesInShot);
		}

		double lengthSum = 0;
		double rmsSum = 0;
		for (Shot shot : shots) {
			lengthSum += shot.getLengthOfShot();
			rmsSum += shot.getRMSMean();
		}
		double averageLength = lengthSum / shots.size();
		double averageRMS = rmsSum / shots.size();
		double sumDifference = 0.0;
		for (Shot shot : shots) {
			sumDifference += Math.abs((shot.getLengthOfShot() * shot.getRMSMean()) - (averageLength * averageRMS));
		}
		double max = (averageLength * averageRMS) + 2 * (sumDifference / shots.size());
		double min = (averageLength * averageRMS) - 2 * (sumDifference / shots.size());
		System.out.println("Number of shots = " + shots.size());
		System.out.println("max: " + max + " min: " + min);
		// TH = (averageW * averageRMS +- 1/n * sumOf(abs(Wi*RMSi -
		// averageW*averageRMS)))
		// where Wi = Li/Ltotal
		for (int i = 0; i < shots.size(); i++) {
			double value = shots.get(i).getLengthOfShot() * shots.get(i).getRMSMean();
			if (value > max) {
				System.out.println("Value of shot " + i + " is out of threshold");
			} else if (value < min) {
				System.out.println("Value of shot " + i + " is out of threshold");
			}
		}
		int count = 1;
		for (Shot s : shots) {
			System.out.println(count + ": " + s.getLengthOfShot() / lengthSum);
			count++;
		}
	}

	private static void findOutliers3() {
		double lengthSum = 0;
		for (Shot shot : shots) {
			lengthSum += shot.getLengthOfShot();
		}
		double averageLength = lengthSum / shots.size();
		double sumDiffSquared = 0;
		for (Shot shot : shots) {
			sumDiffSquared += Math.pow(shot.getLengthOfShot() - averageLength, 2);
		}
		double sd = Math.sqrt(sumDiffSquared / shots.size());
		double max = averageLength + (sd);
		double min = averageLength - (sd);
		System.out.println("length = " + lengthSum);
		System.out.println("average = " + averageLength + " sd = " + sd);
		for (int i = 0; i < shots.size(); i++) {
			double value = shots.get(i).getLengthOfShot();
			if (value < min) {
				System.out.println("Value of shot " + i + " is out of threshold");
			}
		}
	}

	private static void findOutliers4() {
		double lengthSum = 0;
		for (Shot shot : shots) {
			lengthSum += shot.getLengthOfShot();
		}
		System.out.println("length = " + lengthSum);
		for (int i = 0; i < shots.size(); i++) {
			double value = shots.get(i).getLengthOfShot();
			System.out.print("Shot" + (i + 1) + " length = " + value / lengthSum + " ");
			if (value / lengthSum < 0.02) {
				System.out.print("out of threshold");
			}
			System.out.println();
		}
	}
  
    private static int getAmp(byte b0, byte b1) {
        return(((b1 & 0xff) << 8) | (b0 & 0xff));
    }

	private static void makeShots() {
		shots = new ArrayList<Shot>();
		Shot shot = new Shot();

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
		shot.setStartingByte(2157840000L);
		shot.setLengthOfShot(12441600);
		shots.add(shot);
		shot = new Shot();
		shot.setStartingByte(2170281600L);
		shot.setLengthOfShot(11664000);
		shots.add(shot);
		shot = new Shot();
		shot.setStartingByte(2181945600L);
		shot.setLengthOfShot(33825600);
		shots.add(shot);
		shot = new Shot();
		shot.setStartingByte(2215771200L);
		shot.setLengthOfShot(20995200);
		shots.add(shot);

		shot = new Shot();
		shot.setStartingByte(2236766400L);
		shot.setLengthOfShot(36158400);
		shots.add(shot);
		shot = new Shot();
		shot.setStartingByte(2272924800L);
		shot.setLengthOfShot(30715200);
		shots.add(shot);
		shot = new Shot();
		shot.setStartingByte(2303640000L);
		shot.setLengthOfShot(24105600);
		shots.add(shot);
		shot = new Shot();
		shot.setStartingByte(2327745600L);
		shot.setLengthOfShot(5054400);
		shots.add(shot);
		shot = new Shot();
		shot.setStartingByte(2332800000L);
		shot.setLengthOfShot(174960000);
		shots.add(shot);
		shot = new Shot();
		shot.setStartingByte(2507760000L);
		shot.setLengthOfShot(291600000);
		shots.add(shot);
		shot = new Shot();
		shot.setStartingByte(2799360000L);
		shot.setLengthOfShot(699840000);
		shots.add(shot);
	}

	private static void openFile() {
		try {
			File file = new File(INPUT_FILE);
			InputStream inputFileStream = new BufferedInputStream(new FileInputStream(file));
			dis = new DataInputStream(inputFileStream);

			double dataSize = (double) (file.length() - HEADER_SIZE);
			double arraySize = Math.ceil(dataSize / bytesPerFrame);
			rmses = new double[(int) arraySize];
		} catch (FileNotFoundException e) {
			Utilities.die("FileNotFound");
		}
	}

	private static void readData() {
		try {
			dis.skip(44);

			int index = 0;
			int bytesToRead = dis.available();
			while (bytesToRead != 0) {
				if (bytesToRead > bytesPerFrame) {
					bytesToRead = bytesPerFrame;
				}
				int n = 0;
				double sum = 0;
				for (int i = 0; i < (bytesToRead / 2); i++) {
					//int amp = getAmp(dis.readByte(), dis.readByte());
                    int amp = dis.readShort();
					sum += (amp * amp);
					n++;
				}
				rmses[index++] = Math.sqrt(sum / n);
				bytesToRead = dis.available();
			}
		} catch (IOException e) {
			Utilities.die("IOException");
		}
	}

	public static void main(String[] args) {
		makeShots();
		openFile();
		readData();
  for (double d: rmses)
  {
    System.out.println(d);
  }
		//findOutliers5();
		// checkForZeroAmps();
		// bucketSound();
	}

	private static void findOutliers5() {
		long sumLength = 0;
		ArrayList<Double> probAd = new ArrayList<Double>();

		for (Shot shot : shots) {
			long start = shot.getStartingByte();
			long length = shot.getLengthOfShot();
			sumLength += length;
			int framesInShot = (int) (length / BYTES_PER_VIDEO_FRAME);
			double sum = 0;
			for (int i = 0; i < framesInShot; i++) {
				sum += rmses[i];
			}
			shot.setRMSMean(sum / framesInShot);
		}

		System.out.println(sumLength);
		double sumValue = 0;

		for (Shot shot : shots) {
			double value = shot.getRMSMean() / sumLength;
			probAd.add(value);
			System.out.println(shot.getLengthOfShot() + " " + shot.getRMSMean() + " " + value);
//			sumValue += value;
		}

		for(int i = 0 ; i < probAd.size(); i++){
			sumValue += ((probAd.get(i))*(shots.get(i).getLengthOfShot()/BYTES_PER_VIDEO_FRAME));
		}
		
//		double sumDiffSquared = 0;
		double average = sumValue / shots.size();
//		for (Shot shot : shots) {
//			sumDiffSquared += Math.pow((shot.getRMSMean() / sumLength) - average, 2);
//		}

//		double sd = Math.sqrt(sumDiffSquared / shots.size());
		System.out.println(average);
//		System.out.println((.5 * sd));

//		double max = average + (.5 * sd);
//		System.out.println("Max: " + max);
		for (int i = 0; i < shots.size(); i++) {
//			double value = shots.get(i).getRMSMean() / sumLength;
			if (((probAd.get(i))*(shots.get(i).getLengthOfShot()/BYTES_PER_VIDEO_FRAME)) < average) {
				System.out.println("Shot" + (i + 1) + " is out of threshold: " + probAd.get(i));
			}
		}
	}
}
