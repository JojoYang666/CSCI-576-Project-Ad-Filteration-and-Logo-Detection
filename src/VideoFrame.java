public class VideoFrame {
	private int frameNumber;
	private int logoLocation;
	private float confidence;
	private int logo;

	// logo: 0 = starbucks, 1 = subway, 2 = mcd, 3 = nfl
	public VideoFrame() {
		confidence = -1;
		logoLocation = -1;
		logo = -1;
	}

	public VideoFrame(int frameNumber, int logoLocation, float confidence, int logo) {
		super();
		this.frameNumber = frameNumber;
		this.logoLocation = logoLocation;
		this.confidence = confidence;
		this.logo = logo;
	}

	public int getFrameNumber() {
		return frameNumber;
	}

	public void setFrameNumber(int frameNumber) {
		this.frameNumber = frameNumber;
	}

	public int getLogoLocation() {
		return logoLocation;
	}

	public void setLogoLocation(int logoLocation) {
		this.logoLocation = logoLocation;
	}

	public float getConfidence() {
		return confidence;
	}

	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}

	public int getLogo() {
		return logo;
	}

	public void setLogo(int logo) {
		this.logo = logo;
	}
}
