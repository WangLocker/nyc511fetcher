package com.nycdata.fetcher.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nycdata.fetcher.dao.EventDao;
import com.nycdata.fetcher.entity.EventEntity;
import com.nycdata.fetcher.vo.EventVo;
import com.nycdata.fetcher.vo.ScheduleVo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class RegularFetchEvent {
    @Value("${spring.application.apikey}")
    private String apiKey;
    @Value("${spring.application.events.url}")
    private String eventsUrl;
    @Autowired
    private EventDao eventDao;

    private String rawData;
    private HashMap<String,Long> idToLastUpdated = new HashMap<>();
    private HashSet<String> oldEvents = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(RegularFetchEvent.class);

    //@PostConstruct
    public void prepareMap(){
        idToLastUpdated = eventDao.getDatabaseMem();
        oldEvents=new HashSet<>(idToLastUpdated.keySet());
    }

    //@Scheduled(fixedDelay = 300000) // 每300秒执行一次
    public void performTaskAtFixedRate() {
        logger.info("Fetching events from " + eventsUrl);
        getRawData();
        Gson gson = new Gson();
        Type eventType = new TypeToken<List<EventVo>>(){}.getType();
        List<EventVo> events = gson.fromJson(rawData, eventType);

        Set<String> newids = events.stream()
                .map(EventVo::getID) // 提取每个EventVo的ID
                .collect(Collectors.toCollection(HashSet::new)); // 收集到HashSet中

        //交集，看是否更新
        HashSet<String> bothHas=new HashSet<>(oldEvents);
        bothHas.retainAll(newids);

        //old含有new不含有
        Set<String> onlyOldHas = new HashSet<>(oldEvents);
        onlyOldHas.removeAll(newids);

        //new含有old不含有
        Set<String> onlyNewHas = new HashSet<>(newids);
        onlyNewHas.removeAll(oldEvents);

        //准备插入的数据集合List
        List<EventEntity> toInsert = new ArrayList<>();

        // 现在你可以遍历events列表，并访问每个事件的属性
        int update=0;
        for (EventVo event : events) {
            //新旧id集均含有该条目，则只需考虑该事件的记录是否需要更新
            if(bothHas.contains(event.getID())){
                if(idToLastUpdated.get(event.getID())<toUnixTimestamp(event.getLastUpdated())){
                    update++;
                    logger.info("Event "+event.getID()+" has been updated");
                    idToLastUpdated.put(event.getID(),toUnixTimestamp(event.getLastUpdated()));
                    toInsert.add(constructNewEvent(event));
                }
            //只有新id集含有该条目，则需要插入该事件的记录
            }else if (onlyNewHas.contains(event.getID())) {
                oldEvents.add(event.getID());
                idToLastUpdated.put(event.getID(),toUnixTimestamp(event.getLastUpdated()));
                toInsert.add(constructNewEvent(event));
            }
        }

        oldEvents.removeAll(onlyOldHas);

        //插入数据库
        //eventDao.saveEvents(toInsert);

        for (EventEntity eventEntity : toInsert) {
            try {
                eventDao.saveOneEvent(eventEntity);
            }catch (Exception e) {
                logger.error("Error saving event: " + eventEntity);
            }
        }
        logger.info("[UPDATED]: "+update+"  [ADDED]: "+onlyNewHas.size()+"  [DELETED]: "+onlyOldHas.size());

    }

    private EventEntity constructNewEvent(EventVo event) {
        EventEntity newEvent = new EventEntity();
        newEvent.setID_LU(event.getID(),toUnixTimestamp(event.getLastUpdated()));
        newEvent.setRegionName(event.getRegionName());
        newEvent.setCountyName(event.getCountyName());
        newEvent.setSeverity(event.getSeverity());
        newEvent.setRoadwayName(event.getRoadwayName());
        newEvent.setDirectionOfTravel(event.getDirectionOfTravel());
        newEvent.setDescription(event.getDescription());
        newEvent.setLocation(event.getLocation());
        newEvent.setLanesAffected(event.getLanesAffected());
        newEvent.setLaneStatus(event.getLanesStatus());
        newEvent.setPrimaryLocation(event.getPrimaryLocation());
        newEvent.setSecondaryLocation(event.getSecondaryLocation());
        newEvent.setFirstArticleCity(event.getFirstArticleCity());
        newEvent.setSecondCity(event.getSecondCity());
        newEvent.setEventType(event.getEventType());
        newEvent.setEventSubType(event.getEventSubType());
        newEvent.setLatitude(event.getLatitude());
        newEvent.setLongitude(event.getLongitude());
        newEvent.setPlannedEndDate(toUnixTimestamp(event.getPlannedEndDate()));
        newEvent.setReported(toUnixTimestamp(event.getReported()));
        newEvent.setStartDate(toUnixTimestamp(event.getStartDate()));
        newEvent.setSchedule(toJson(event.getSchedule()));
        return newEvent;
    }

    public String toJson(List<ScheduleVo> schedule) {
        Gson gson = new Gson();
        return gson.toJson(schedule);
    }


    public void getRawData() {
        String urlString = eventsUrl+"?key={"+apiKey+"}&format={json}";
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();


            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                rawData=response.toString();

            } else {
                logger.error("GET request not worked");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long toUnixTimestamp(String date) {
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of("America/New_York"));
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, formatter);
            // 转换为UNIX时间戳
            long unixTimestamp = zonedDateTime.toEpochSecond();

            return unixTimestamp;
        }catch (Exception e) {
            return 0;
        }
    }
}
