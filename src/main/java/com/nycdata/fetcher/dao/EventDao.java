package com.nycdata.fetcher.dao;

import com.nycdata.fetcher.entity.EventEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
@Service
public class EventDao {
    @Autowired
    private EventRepository eventRepository;

    public void saveEvents(List<EventEntity> toInsert) {
        eventRepository.saveAll(toInsert);
    }
    public void saveOneEvent(EventEntity toInsert) {
        eventRepository.save(toInsert);
    }

    public HashMap<String,Long> getDatabaseMem(){
        HashMap<String,Long> idToLastUpdated = new HashMap<>();
        List<Object[]> idAndLastUpdated = eventRepository.findMaxLastUpdatedByEachId();
        for (Object[] idAndLast : idAndLastUpdated) {
            idToLastUpdated.put((String) idAndLast[0], (long) idAndLast[1]);
        }
        return idToLastUpdated;
    }
}
