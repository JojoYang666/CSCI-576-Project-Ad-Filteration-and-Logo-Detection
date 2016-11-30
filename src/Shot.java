public class Shot{
    private boolean isAd;
	private double yMean;
	private double rmsMean;
	private long startingByte;
	private long lengthOfShot;
    private int audioVoteCount;
    private long startingFrame;
	private long endingFrame;
	
	public Shot() {
		this.audioVoteCount = 0;
		this.isAd = false;
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
		return audioVoteCount;
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
}