package com.zhongbenshuo.air.bean;

public class Station implements Comparable<Station> {

    private int stationId;

    private String stationName;

    private boolean online;

    public Station(int stationId, String stationName, boolean online) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.online = online;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Station)) {
            return false;
        } else {
            try {
                Station that = (Station) o;
                return stationId == that.stationId;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public int compareTo(Station o) {
        return this.stationId - o.stationId;
    }
}
