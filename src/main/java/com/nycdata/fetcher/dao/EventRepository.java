package com.nycdata.fetcher.dao;

import com.nycdata.fetcher.entity.EventEntity;
import com.nycdata.fetcher.entity.EventKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, EventKey> {
    @Query("SELECT e.ID_LU.ID, MAX(e.ID_LU.lastupdated) FROM EventEntity e GROUP BY e.ID_LU.ID")
    List<Object[]> findMaxLastUpdatedByEachId();

}
