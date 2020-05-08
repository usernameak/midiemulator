package nl.deltacxx.insomnia247.midiemulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PianoWidget extends JComponent {
    private int[] pressedNotes;

    public PianoWidget(int[] pressedNotes) {
        super();
        this.pressedNotes = pressedNotes;
        setPreferredSize(new Dimension(14 * 32, 128));
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                requestFocusInWindow();
            }
        });
    }

    private static final boolean[] blackKeys = new boolean[] {false, true, true, false, true, true, true};
    private static final int[] keyOrdinals = new int[] {0, -1, 1, -2, 2, 3, -4, 4, -5, 5, -6, 6};

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics g = graphics.create();

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());

        for(int i = 0; i < 24; i++) {
            if(pressedNotes[i] != -1 && keyOrdinals[i % 12] >= 0) {
                int x = keyOrdinals[i % 12] + i / 12 * 7;
                g.setColor(Color.red);
                g.fillRect(x * 32, 0, 32, getHeight());
            }
        }

        for(int i = 0; i < 14; i++) {
            g.setColor(Color.black);
            g.drawLine(i * 32, 0, i * 32, getHeight());

            if(blackKeys[i % blackKeys.length]) {
                g.fillRect(i * 32 - 8, 0, 16, getHeight() - 32);
            }
        }

        for(int i = 0; i < 24; i++) {
            if(pressedNotes[i] != -1 && keyOrdinals[i % 12] < 0) {
                int ord = -keyOrdinals[i % 12];
                int x = ord + i / 12 * 7;
                g.setColor(Color.red);
                g.fillRect(x * 32 - 8, 0, 16, getHeight() - 32);
            }
        }
    }

}
