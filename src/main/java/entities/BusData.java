package entities;

import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class BusData implements Serializable {

    private int busNumber;

    @NotEmpty
    private String busName;

    @NumberFormat(pattern = "###.#")
    private Double busLoad;

    private String deviceName;


    public int getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(int busNumber) {
        this.busNumber = busNumber;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public Double getBusLoad() {
        return busLoad;
    }

    public void setBusLoad(Double busLoad) {
        this.busLoad = busLoad;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return this.busNumber + "  " + this.busName +"  "+ this.busLoad +"  "+ this.deviceName ;
    }
}
