package com.nycdata.fetcher.vo;

import java.util.List;

public class EventVo {
    private String LastUpdated;
    private float Latitude;
    private float Longitude;
    private String PlannedEndDate;
    private String Reported;
    private String StartDate;
    private List<ScheduleVo> Schedule;
    private String ID;
    private String RegionName;
    private String CountyName;
    private String Severity;
    private String RoadwayName;
    private String DirectionOfTravel;
    private String Description;
    private String Location; // 注意这里是null，确保你的应用能处理null值
    private String LanesAffected;
    private String LanesStatus;
    private String LcsEntries;
    private String NavteqLinkId;
    private String PrimaryLocation;
    private String SecondaryLocation;
    private String FirstArticleCity;
    private String SecondCity;
    private String EventType;
    private String EventSubType;
    private String MapEncodedPolyline;
    public String getLastUpdated() {
        return LastUpdated;
    }
    public float getLatitude() {
        return Latitude;
    }
    public float getLongitude() {
        return Longitude;
    }
    public String getPlannedEndDate() {
        return PlannedEndDate;
    }
    public String getReported() {
        return Reported;
    }
    public String getStartDate() {
        return StartDate;
    }
    public List<ScheduleVo> getSchedule() {
        return Schedule;
    }
    public String getID() {
        return ID;
    }
    public String getRegionName() {
        return RegionName;
    }
    public String getCountyName() {
        return CountyName;
    }
    public String getSeverity() {
        return Severity;
    }
    public String getRoadwayName() {
        return RoadwayName;
    }
    public String getDirectionOfTravel() {
        return DirectionOfTravel;
    }
    public String getDescription() {
        return Description;
    }
    public String getLocation() {
        return Location;
    }
    public String getLanesAffected() {
        return LanesAffected;
    }
    public String getLanesStatus() {
        return LanesStatus;
    }
    public String getLcsEntries() {
        return LcsEntries;
    }
    public String getNavteqLinkId() {
        return NavteqLinkId;
    }
    public String getPrimaryLocation() {
        return PrimaryLocation;
    }
    public String getSecondaryLocation() {
        return SecondaryLocation;
    }
    public String getFirstArticleCity() {
        return FirstArticleCity;
    }
    public String getSecondCity() {
        return SecondCity;
    }
    public String getEventType() {
        return EventType;
    }
    public String getEventSubType() {
        return EventSubType;
    }
    public String getMapEncodedPolyline() {
        return MapEncodedPolyline;
    }

}
