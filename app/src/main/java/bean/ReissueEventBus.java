package bean;

public class ReissueEventBus {

    byte [] data;

    public ReissueEventBus(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
