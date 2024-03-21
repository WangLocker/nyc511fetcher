package com.nycdata.fetcher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class EventKey implements Serializable {
    @Column(name = "ID")
    private String ID;
    @Column(name = "lastupdated")
    private long lastupdated;

    public EventKey(String ID, long lastupdated) {
        this.ID = ID;
        this.lastupdated = lastupdated;
    }
    public EventKey() {
    }

}
