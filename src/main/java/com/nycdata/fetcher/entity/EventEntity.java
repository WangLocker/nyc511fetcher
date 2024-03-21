package com.nycdata.fetcher.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class EventEntity {

    @EmbeddedId
    //事件唯一标识符
    private EventKey ID_LU;

    @Column(name = "regionname")
    //事件发生地的区
    private String RegionName;

    @Column(name = "countyname")
    //事件发生地的县
    private String CountyName;

    @Column(name = "severity")
    //事件严重程度，默认值为unknown
    private String Severity;

    @Column(name = "roadwayname")
    //事件发生地的道路名称
    private String RoadwayName;

    @Column(name = "directionoftravel")
    //以下行驶方向之一：无，所有方向，北行，东行，南行，西行，入境，出境，出入两个方向
    private String DirectionOfTravel;

    @Column(name = "description")
    //事件描述，包括事件类型、日期和受影响的车道
    private String Description;

    @Column(name = "location")
    //描述事件发生的道路上的位置。例如，事件发生在道路的左侧或右侧
    private String Location;

    @Column(name = "lanesaffected")
    //描述受事件影响的车道或车道数。例如：所有车道
    private String LanesAffected;

    @Column(name = "lanestatus")
    //描述受事件影响的车道的状态。例如：关闭
    private String LaneStatus;

    @Column(name = "primarylocation")
    //描述受事件影响的路段的起点
    private String PrimaryLocation;

    @Column(name = "secondarylocation")
    //描述受事件影响的路段的终点
    private String SecondaryLocation;

    @Column(name = "firstarticlecity")
    //受影响道路段起点的城市，可能包含文章中的城市，例如：New York之间
    private String FirstArticleCity;

    @Column(name = "secondcity")
    //受影响道路段终点的城市
    private String SecondCity;

    @Column(name = "eventtype")
    //事件的类型，事故和事件、道路工程、特殊事件、关闭、传输模式、通用信息、冬季驾驶索引
    private String EventType;

    @Column(name = "eventsubtype")
    //更详细和描述性的事件类型。没有固定的子类型列表
    private String EventSubType;

    @Column(name = "latitude")
    //事件发生的纬度
    private float Latitude;

    @Column(name = "longitude")
    //事件发生的经度
    private float Longitude;

    @Column(name = "plannedenddate")
    //事件预计的结束时间，格式：dd/MM/yyyy HH：mm：ss
    private long PlannedEndDate;

    @Column(name = "reported")
    //事件被报告的时间
    private long Reported;

    @Column(name = "startdate")
    //事件的开始时间，格式：dd/MM/yyyy HH：mm：ss
    private long StartDate;

    @Column(name = "schedule")
    //事件的每日排期
    private String Schedule;

    public void setID_LU(String id, long lastupdated){
        this.ID_LU = new EventKey(id,lastupdated);
    }

    public void  setRegionName(String regionName){
        this.RegionName = regionName;
    }

    public void setCountyName(String countyName){
        this.CountyName = countyName;
    }

    public void setSeverity(String severity){
        this.Severity = severity;
    }

    public void setRoadwayName(String roadwayName){
        this.RoadwayName = roadwayName;
    }

    public void setDirectionOfTravel(String directionOfTravel){
        this.DirectionOfTravel = directionOfTravel;
    }

    public void setDescription(String description){
        this.Description = description;
    }

    public void setLocation(String location){
        this.Location = location;
    }

    public void setLanesAffected(String lanesAffected){
        this.LanesAffected = lanesAffected;
    }

    public void setLaneStatus(String laneStatus){
        this.LaneStatus = laneStatus;
    }

    public void setPrimaryLocation(String primaryLocation){
        this.PrimaryLocation = primaryLocation;
    }

    public void setSecondaryLocation(String secondaryLocation){
        this.SecondaryLocation = secondaryLocation;
    }

    public void setFirstArticleCity(String firstArticleCity){
        this.FirstArticleCity = firstArticleCity;
    }

    public void setSecondCity(String secondCity){
        this.SecondCity = secondCity;
    }

    public void setEventType(String eventType){
        this.EventType = eventType;
    }

    public void setEventSubType(String eventSubType){
        this.EventSubType = eventSubType;
    }

    public void setLatitude(float latitude){
        this.Latitude = latitude;
    }

    public void setLongitude(float longitude){
        this.Longitude = longitude;
    }

    public void setPlannedEndDate(long plannedEndDate){
        this.PlannedEndDate = plannedEndDate;
    }

    public void setReported(long reported){
        this.Reported = reported;
    }

    public void setStartDate(long startDate){
        this.StartDate = startDate;
    }

    public void setSchedule(String s){
        this.Schedule = s;
    }

}
