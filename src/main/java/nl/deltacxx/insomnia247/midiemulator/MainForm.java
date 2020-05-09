package nl.deltacxx.insomnia247.midiemulator;

import com.sun.jna.ptr.PointerByReference;
import com.sun.media.sound.MidiUtils;
import net.miginfocom.swing.MigLayout;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainForm extends JFrame implements KeyListener {
    private static final int[] keymap = new int[]{
            // bass keys
            KeyEvent.VK_DELETE,
            KeyEvent.VK_HOME, //
            KeyEvent.VK_END,
            KeyEvent.VK_PAGE_UP, //
            KeyEvent.VK_PAGE_DOWN,
            KeyEvent.VK_NUMPAD7,
            KeyEvent.VK_DIVIDE, //
            KeyEvent.VK_NUMPAD8,
            KeyEvent.VK_MULTIPLY, //
            KeyEvent.VK_NUMPAD9,
            KeyEvent.VK_SUBTRACT, //
            KeyEvent.VK_ADD,
            // melody keys
            KeyEvent.VK_Q,
            KeyEvent.VK_2, //
            KeyEvent.VK_W,
            KeyEvent.VK_3, //
            KeyEvent.VK_E,
            KeyEvent.VK_R,
            KeyEvent.VK_5, //
            KeyEvent.VK_T,
            KeyEvent.VK_6, //
            KeyEvent.VK_Y,
            KeyEvent.VK_7, //
            KeyEvent.VK_U,
            // melody second octave
            KeyEvent.VK_I,
            KeyEvent.VK_9,//
            KeyEvent.VK_O,
            KeyEvent.VK_0,//
            KeyEvent.VK_P,
            KeyEvent.VK_OPEN_BRACKET,
            KeyEvent.VK_EQUALS, //
            KeyEvent.VK_CLOSE_BRACKET,
    };

    private static final Map<Integer, Integer> reverseKeymap;
    private JSpinner octaveSelector;

    private int[] pressedNotes = new int[32];

    private MidiDeviceEntry selectedMidiDevice;

    static {
        Map<Integer, Integer> reverseKeymap_ = new HashMap<>();
        for (int i = 0; i < keymap.length; i++) {
            reverseKeymap_.put(keymap[i], i);
        }
        reverseKeymap = Collections.unmodifiableMap(reverseKeymap_);
    }

    {
        for(int i = 0; i < pressedNotes.length; i++) {
            pressedNotes[i] = -1;
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new MainForm().setVisible(true));
    }

    private PianoWidget pianoWidget;
    private JComboBox<MidiDeviceEntry> deviceSelector;
    private List<MidiDeviceEntry> midiDeviceList = new ArrayList<>();

    private void initMidi() {
        if (selectedMidiDevice != null) {
            deinitMidiDevice(selectedMidiDevice);
        }

        int deviceCount = PortmidiLibrary.INSTANCE.Pm_CountDevices();
        for (int i = 0; i < deviceCount; i++) {
            PortmidiLibrary.PmDeviceInfo info = PortmidiLibrary.INSTANCE.Pm_GetDeviceInfo(i);
            if (info.output != 0) {
                MidiDeviceEntry entry = new MidiDeviceEntry(i, info.name.getString(0L));
                midiDeviceList.add(entry);
                if (selectedMidiDevice == null) {
                    selectedMidiDevice = entry;
                }
            }
        }

        initMidiDevice();
    }

    private void initMidiDevice() {
        PointerByReference ptr = new PointerByReference();
        PortmidiLibrary.INSTANCE.Pm_OpenOutput(ptr, selectedMidiDevice.getId(), null, 0, null, null, 1);
        selectedMidiDevice.setStream(new PortmidiLibrary.PortMidiStream(ptr.getValue()));
    }

    private void deinitMidiDevice(MidiDeviceEntry device) {
        if (device.getStream() != null) {
            PortmidiLibrary.INSTANCE.Pm_Close(device.getStream());
            device.setStream(null);
        }
    }

    public MainForm() {
        super();

        setTitle("MidiEmulator");

        initMidi();

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new MigLayout("ins 0", "8[]8[]8", "8[]8[]8[]8"));
        deviceSelector = new JComboBox<>();
        deviceSelector.setModel(new ComboBoxModel<MidiDeviceEntry>() {


            @Override
            public void setSelectedItem(Object o) {
                if (selectedMidiDevice != null) {
                    deinitMidiDevice(selectedMidiDevice);
                }
                selectedMidiDevice = (MidiDeviceEntry) o;
                initMidiDevice();
            }

            @Override
            public Object getSelectedItem() {
                return selectedMidiDevice;
            }

            @Override
            public int getSize() {
                return midiDeviceList.size();
            }

            @Override
            public MidiDeviceEntry getElementAt(int i) {
                return midiDeviceList.get(i);
            }

            @Override
            public void addListDataListener(ListDataListener listDataListener) {

            }

            @Override
            public void removeListDataListener(ListDataListener listDataListener) {

            }
        });
        contentPane.add(deviceSelector, "wrap,grow,span 2");
        octaveSelector = new JSpinner(new SpinnerNumberModel(3, -2, 8, 1));
        contentPane.add(octaveSelector, "wrap,grow");
        contentPane.add(pianoWidget = new PianoWidget(pressedNotes), "span 2");
        setContentPane(contentPane);


        pack();

        pianoWidget.addKeyListener(this);
        pianoWidget.setFocusable(true);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    private int buildMidiMessage(int status, int data1, int data2) {
        return ((((data2) << 16) & 0xFF0000) |
                (((data1) << 8) & 0xFF00) |
                ((status) & 0xFF));
    }

    private void noteOn(int note) {
        PortmidiLibrary.PmEvent event = new PortmidiLibrary.PmEvent();
        event.message = buildMidiMessage(0b10010000, note, 100);
        event.timestamp = (int) System.currentTimeMillis();
        PortmidiLibrary.INSTANCE.Pm_Write(selectedMidiDevice.getStream(), event, 1);
    }

    private void noteOff(int note) {
        PortmidiLibrary.PmEvent event = new PortmidiLibrary.PmEvent();
        event.message = buildMidiMessage(0b10000000, note, 100);
        event.timestamp = (int) System.currentTimeMillis();
        PortmidiLibrary.INSTANCE.Pm_Write(selectedMidiDevice.getStream(), event, 1);
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        Integer note = reverseKeymap.get(keyEvent.getKeyCode());
        if (note != null) {
            int midiNote = note + (((int) octaveSelector.getValue()) + 2 - 1) * 12;
            if(pressedNotes[note] != -1 && pressedNotes[note] != midiNote) {
                noteOff(midiNote);
            }
            if(pressedNotes[note] == -1) {
                PortmidiLibrary.PmEvent event = new PortmidiLibrary.PmEvent();
                event.message = buildMidiMessage(0b10010000, midiNote, 100);
                event.timestamp = (int) System.currentTimeMillis();
                PortmidiLibrary.INSTANCE.Pm_Write(selectedMidiDevice.getStream(), event, 1);
                pressedNotes[note] = midiNote;
                pianoWidget.repaint();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        Integer note = reverseKeymap.get(keyEvent.getKeyCode());
        if (note != null) {
            // int midiNote = note + (((int) octaveSelector.getValue()) + 2) * 12;
            if (pressedNotes[note] != -1) {
                noteOff(pressedNotes[note]);
                pressedNotes[note] = -1;
                pianoWidget.repaint();
            }
        }
    }
}
