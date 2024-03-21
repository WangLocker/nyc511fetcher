package com.nycdata.fetcher.vo;

public class CameraVo {
    private Double Latitude;
    private Double Longitude;
    private String ID;
    private String Name;
    private String DirectionOfTravel;
    private String RoadwayName;
    private String Url;
    private String VideoUrl;
    private boolean Disabled;
    private boolean Blocked;

    public Double getLatitude() {
        return Latitude;
    }
    public Double getLongitude() {
        return Longitude;
    }
    public String getID() {
        return ID;
    }
    public String getName() {
        return Name;
    }
    public String getDirectionOfTravel() {
        return DirectionOfTravel;
    }
    public String getRoadwayName() {
        return RoadwayName;
    }
    public String getUrl() {
        return Url;
    }
    public String getVideoUrl() {
        return VideoUrl;
    }
    public boolean getDisabled() {
        return Disabled;
    }
    public boolean getBlocked() {
        return Blocked;
    }
    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }
    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }
    public void setID(String iD) {
        ID = iD;
    }
    public void setName(String name) {
        Name = name;
    }
    public void setDirectionOfTravel(String directionOfTravel) {
        DirectionOfTravel = directionOfTravel;
    }
    public void setRoadwayName(String roadwayName) {
        RoadwayName = roadwayName;
    }
    public void setUrl(String url) {
        Url = url;
    }
    public void setVideoUrl(String videoUrl) {
        VideoUrl = videoUrl;
    }
    public void setDisabled(boolean disabled) {
        Disabled = disabled;
    }
    public void setBlocked(boolean blocked) {
        Blocked = blocked;
    }

}
