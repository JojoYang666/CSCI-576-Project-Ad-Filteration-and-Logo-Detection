

public class Shot{
    private boolean isAd;
	private double yMean;
	private double rmsMean;
	private long startingByte;
	private long lengthOfShot;
	
	public Shot(double yMean, double rmsMean, long startingByte, long lengthOfShot, boolean isAd) {
		super();
		this.yMean = yMean;
		this.rmsMean = rmsMean;
		this.startingByte = startingByte;
		this.lengthOfShot = lengthOfShot;
        this.isAd = isAd;
	}
	
	public Shot(){
		
	}
  
    public boolean getIsAd() {
		return isAd;
	}

	public void setIsAd(boolean isAd) {
		this.isAd = isAd;
	}
	
	public double getyMean() {
		return yMean;
	}

	public void setyMean(double yMean) {
		this.yMean = yMean;
	}

	public double getRMSMean() {
		return rmsMean;
	}

	public void setRMSMean(double rmsMean) {
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
	
}