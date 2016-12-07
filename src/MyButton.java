

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Youngmin on 2016. 11. 11..
 */

public class MyButton extends JButton {

    public MyButton(String label){
        setFont(new Font("Helvetica", Font.BOLD, 10));
        setText(label);
        addMouseListener(
                new MouseAdapter() {
                    public void mousePressed(MouseEvent e)
                    {
                        buttonPressed(getText());
                    }
                }
        );
    }

    public void buttonPressed(String label)
    {
        if(label.equals("Play")) { // Play
            PlayVideo.resume();
            PlaySound.resume();
        }

        else if(label.equals("Pause")) { // Pause
            PlayVideo.suspend();
            PlaySound.suspend();
        }

        else if (label.equals("Stop")) { // Stop
            PlayVideo.stop();
            PlaySound.stop();
        }
    }
}

