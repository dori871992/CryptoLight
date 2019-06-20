package com.dorianmusaj.cryptolight;

public class SensorData {

    private String tempData;
    private String lightData;
    private String proximityData;
    private String accelerometerData;
    private String humidityData;
    private String pressureData;


    public String getTempData() {
        return tempData;
    }

    public void setTempData(String tempData) {
        this.tempData = tempData;
    }

    public String getLightData() {
        return lightData;
    }

    public void setLightData(String lightData) {
        this.lightData = lightData;
    }

    public String getProximityData() {
        return proximityData;
    }

    public void setProximityData(String proximityData) {
        this.proximityData = proximityData;
    }

    public String getAccelerometerData() {
        return accelerometerData;
    }

    public void setAccelerometerData(String accelerometerData) {
        this.accelerometerData = accelerometerData;
    }

    public String getHumidityData() {
        return humidityData;
    }

    public void setHumidityData(String humidityData) {
        this.humidityData = humidityData;
    }

    public String getPressureData() {
        return pressureData;
    }

    public void setPressureData(String pressureData) {
        this.pressureData = pressureData;
    }
}