import java.util.ArrayList;

public class VideoFrame {
    private int frameNumber;
    private int[] logoLocations;
  
    public VideoFrame() {}
  
    public VideoFrame(int frameNum) {
        this.frameNumber = frameNum;
        this.logoLocations = new int[] {-1, -1, -1, -1};
    }
  
    public int getFrameNumber() {
        return(this.frameNumber);
    }
  
    public void setFrameNumber(int num) {
        this.frameNumber = num;
    }
  
    public int[] getLogoLocations() {
        return(this.logoLocations);
    }
  
    // logo: 0 = starbucks, 1 = subway, 2 = mcd, 3 = nfl
    public void addLogoLocation(int logo, int location) {
        this.logoLocations[logo] = location;
    }
}
