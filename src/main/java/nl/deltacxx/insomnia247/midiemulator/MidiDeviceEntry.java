package nl.deltacxx.insomnia247.midiemulator;

import java.util.Objects;

public class MidiDeviceEntry {
    private int id;
    private String name;
    private PortmidiLibrary.PortMidiStream stream;

    public MidiDeviceEntry(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PortmidiLibrary.PortMidiStream getStream() {
        return stream;
    }

    public void setStream(PortmidiLibrary.PortMidiStream stream) {
        this.stream = stream;
    }

    @Override
    public String toString() {
        return "MidiDeviceEntry{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MidiDeviceEntry that = (MidiDeviceEntry) o;
        return id == that.id &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
