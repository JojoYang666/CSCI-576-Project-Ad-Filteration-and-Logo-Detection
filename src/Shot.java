

public class Shot{
	private double yMean;
	private double ampMean;
	private long startingByte;
	private long lengthOfShot;
	
	public Shot(double yMean, double ampMean, long startingByte, long lengthOfShot) {
		super();
		this.yMean = yMean;
		this.ampMean = ampMean;
		this.startingByte = startingByte;
		this.lengthOfShot = lengthOfShot;
	}
	
	public Shot(){
		
	}
	
	public double getyMean() {
		return yMean;
	}

	public void setyMean(double yMean) {
		this.yMean = yMean;
	}

	public double getAmpMean() {
		return ampMean;
	}

	public void setAmpMean(double ampMean) {
		this.ampMean = ampMean;
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