

public class Scene{
	private long startingByte;
	private long lengthOfScene;
	//private String nameOfLogo;
	
	public Scene(long startingByte, long lengthOfScene) {
		super();
		this.startingByte = startingByte;
		this.lengthOfScene = lengthOfScene;
	}
	
	public long getStartingByte() {
		return startingByte;
	}
	public void setStartingByte(long startingByte) {
		this.startingByte = startingByte;
	}
	public long getLengthOfScene() {
		return lengthOfScene;
	}
	public void setLengthOfScene(long lengthOfScene) {
		this.lengthOfScene = lengthOfScene;
	}
	
}