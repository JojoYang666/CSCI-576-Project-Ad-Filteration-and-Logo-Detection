import java.util.ArrayList;

public class Shot{
    private boolean isAd;
	private double yMean;
	private double rmsMean;
	private long startingByte;
	private long lengthOfShot;
    private int audioVoteCount;
    private long startingFrame;
	private long endingFrame;
    private long numberOfFrames;
    private ArrayList<VideoFrame> framesWithLogo;
	
	public Shot() {
		this.audioVoteCount = 0;
		this.isAd = false;
        this.framesWithLogo = new ArrayList<VideoFrame>();
	}
	
	public Shot(boolean isAd, double yMean, double rmsMean, long startingByte, long lengthOfShot, int audioVoteCount,
			long startingFrame, long endingFrame) {
		super();
		this.isAd = isAd;
		this.yMean = yMean;
		this.rmsMean = rmsMean;
		this.startingByte = startingByte;
		this.lengthOfShot = lengthOfShot;
		this.audioVoteCount = audioVoteCount;
		this.startingFrame = startingFrame;
		this.endingFrame = endingFrame;
        this.numberOfFrames = endingFrame - startingFrame;
        this.framesWithLogo = new ArrayList<VideoFrame>();
	}

	public boolean isAd() {
		return isAd;
	}
	
	public void setAd(boolean isAd) {
		this.isAd = isAd;
	}
	
	public double getyMean() {
		return yMean;
	}
	
	public void setyMean(double yMean) {
		this.yMean = yMean;
	}
	
	public double getRmsMean() {
		return rmsMean;
	}
	
	public void setRmsMean(double rmsMean) {
		this.rmsMean = rmsMean;
	}
	
	public long getStartingByte() {
		return startingByte;
	}
	
	public void setStartingByte(long startingByte) {
		this.startingByte = startingByte;
	}
	
	public long getLengthOfShot() {
		return lengthOfShot;
	}
	
	public void setLengthOfShot(long lengthOfShot) {
		this.lengthOfShot = lengthOfShot;
	}
	
	public int getAudioVoteCount() {
		return this.audioVoteCount;
	}
  
    public void incrementAudioVoteCount() {
        this.audioVoteCount++;
    }
	
	public void setAudioVoteCount(int audioVoteCount) {
		this.audioVoteCount = audioVoteCount;
	}
	
	public long getStartingFrame() {
		return startingFrame;
	}
	
	public void setStartingFrame(long startingFrame) {
		this.startingFrame = startingFrame;
	}
	
	public long getEndingFrame() {
		return endingFrame;
	}
	
	public void setEndingFrame(long endingFrame) {
		this.endingFrame = endingFrame;
	}
  
    public long getNumberOfFrames() {
        return this.numberOfFrames;
    }
  
    public void setNumberOfFrames(long num) {
        this.numberOfFrames = num;
    }
  
    public ArrayList<VideoFrame> getFramesWithLogo() {
        return(this.framesWithLogo);
    }
  
    public void addFramesWithLogo(VideoFrame frame) {
        this.framesWithLogo.add(frame);
    }
}