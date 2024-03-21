package com.nycdata.fetcher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cameras")
public class CameraEntity {
    @EmbeddedId
    private EventKey ID_LU;

    @Column(name = "name")
    private String Name;

    @Column(name = "latitude")
    private double Latitude;

    @Column(name = "longitude")
    private double Longitude;

    @Column(name = "roadwayname")
    private String RoadwayName;

    @Column(name = "directionoftravel")
    private String DirectionOfTravel;

    @Column(name = "type")
    private int Type;

    @Column(name = "path")
    private String Path;

    public void setID_LU(String id, long lastupdated){
        this.ID_LU = new EventKey(id,lastupdated);
    }
    public EventKey getID_LU(){
        return this.ID_LU;
    }
    public void setName(String name){
        this.Name = name;
    }
    public String getName(){
        return this.Name;
    }
    public void setLatitude(double latitude){
        this.Latitude = latitude;
    }
    public double getLatitude(){
        return this.Latitude;
    }
    public void setLongitude(double longitude){
        this.Longitude = longitude;
    }
    public double getLongitude(){
        return this.Longitude;
    }
    public void setRoadwayName(String roadwayName){
        this.RoadwayName = roadwayName;
    }
    public String getRoadwayName(){
        return this.RoadwayName;
    }
    public void setDirectionOfTravel(String directionOfTravel){
        this.DirectionOfTravel = directionOfTravel;
    }
    public String getDirectionOfTravel(){
        return this.DirectionOfTravel;
    }
    public void setType(int type){
        this.Type = type;
    }
    public int getType(){
        return this.Type;
    }
    public void setPath(String path){
        this.Path = path;
    }
    public String getPath(){
        return this.Path;
    }

}
