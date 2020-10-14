package bean;

import org.litepal.crud.LitePalSupport;

public class Reissue extends LitePalSupport {

    String Blename;
    int number;
    String data;

    String detailData;

    public String getDetailData() {
        return detailData;
    }

    public void setDetailData(String detailData) {
        this.detailData = detailData;
    }

    public Reissue(String blename, int number,String detailData, String data) {
        Blename = blename;
        this.number = number;
        this.data = data;
        this.detailData =detailData;
    }

    public void setBlename(String blename) {
        Blename = blename;
    }

    @Override
    public String toString() {
        return "Reissue{" +
                "Blename='" + Blename + '\'' +
                ", number=" + number +
                ", data='" + data + '\'' +
                '}';
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getBlename() {
        return Blename;
    }

    public int getNumber() {
        return number;
    }

    public String getData() {
        return data;
    }
}
