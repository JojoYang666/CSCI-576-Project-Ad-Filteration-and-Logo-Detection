import java.awt.*;
import java.awt.image.*;
import javax.swing.JComponent;

public class PlayVideoComponent extends JComponent {

    private BufferedImage img;

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(img,0,0,this);
    }

    public void setImg(BufferedImage img) {
        this.img = img;
    }

}