package com.nycdata.fetcher.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nycdata.fetcher.dao.CameraDao;
import com.nycdata.fetcher.entity.CameraEntity;
import com.nycdata.fetcher.vo.CameraVo;
import jakarta.annotation.PostConstruct;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
@Service
public class RegularFetchCamera {
    @Value("${spring.application.apikey}")
    private String apiKey;

    @Value("${spring.application.cameras.url}")
    private String camerasUrl;

    @Value("${spring.application.cameras.imgpath}")
    private String imgPath;

    @Value("${spring.application.cameras.vdopath}")
    private String vdoPath;

    @Autowired
    private CameraDao cameraDao;

    private String rawData;
    private Polygon nycPolygon;

    @PostConstruct
    public void init() {
        GeometryFactory geometryFactory = new GeometryFactory();

        // 假设这是纽约市区的简化边界（实际情况下，纽约市的边界要复杂得多）
        Coordinate[] coords = new Coordinate[]{
                new Coordinate(-73.92665, 40.931135),
                new Coordinate(-74.0657054, 40.6674887),
                new Coordinate(-74.2047612, 40.654908),
                new Coordinate(-74.290236, 40.463001),
                new Coordinate(-73.726358, 40.5086045),
                new Coordinate(-73.6702256,40.742923),
                new Coordinate(-73.7416671, 40.88582),
                new Coordinate(-73.92665, 40.931135)
        };
        nycPolygon = geometryFactory.createPolygon(coords);
    }

    private static final Logger logger = LoggerFactory.getLogger(RegularFetchCamera.class);

    @Scheduled(fixedRate = 360000) // 每360秒执行一次
    public void performTask1AtFixedRate() {
        //multi thread
        ExecutorService executor = Executors.newFixedThreadPool(8);
        AtomicInteger imageCounter=new AtomicInteger(0);

        logger.info("[***CAMERA0***]Fetching cameras from " + camerasUrl);
        getRawData();
        Gson gson = new Gson();
        Type eventType = new TypeToken<List<CameraVo>>(){}.getType();
        List<CameraVo> cameras = gson.fromJson(rawData, eventType);

        //过滤摄像头
        List<CameraVo> okCameras=getFilteredCameras(cameras);
        logger.info("[***CAMERA0***]Number of enabled cameras in NYC: "+okCameras.size());

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        //对于每个enabled且在纽约市境内的摄像头，若url不为null保存其图片，若videourl不为null保存其视频

        for (CameraVo camera : okCameras) {
            if (camera.getUrl() != null) {
                String imageUrl = camera.getUrl();
                String destinationFile = imgPath + "/" +camera.getID().replace("-","_") +"_"+System.currentTimeMillis()/1000+ ".jpg";
                executor.submit(new ImgTask(camera, imageUrl, destinationFile, cameraDao, logger, imageCounter));
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        logger.info("[***CAMERA0***]Time taken to fetch and save images and videos: " + (endTime - startTime) + "ms");
        logger.info("[***CAMERA0***]Number of images saved: " + imageCounter.get());
    }


    @Scheduled(fixedRate = 360000) // 每600秒执行一次
    public void performTask2AtFixedRate() {
        //multi thread
        ExecutorService executor = Executors.newFixedThreadPool(24);
        AtomicInteger videoCounter=new AtomicInteger(0);

        logger.info("[***CAMERA1***]Fetching cameras from " + camerasUrl);
        getRawData();
        Gson gson = new Gson();
        Type eventType = new TypeToken<List<CameraVo>>(){}.getType();
        List<CameraVo> cameras = gson.fromJson(rawData, eventType);

        //过滤摄像头
        List<CameraVo> okCameras=getFilteredCameras(cameras);
        logger.info("[***CAMERA1***]Number of enabled cameras in NYC: "+okCameras.size());

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        //对于每个enabled且在纽约市境内的摄像头，若url不为null保存其图片，若videourl不为null保存其视频
        for (CameraVo camera : okCameras) {
            if (camera.getVideoUrl() != null) {
                //视频URL与视频保存路径
                String videoUrl = camera.getVideoUrl();
                String destinationFile = vdoPath + "/"+camera.getID().replace("-","_") +"_"+System.currentTimeMillis()/1000+ ".mp4";
                executor.submit(new VdoTask(camera, videoUrl, destinationFile, cameraDao, logger, videoCounter));
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        logger.info("[***CAMERA1***]Time taken to fetch and save images and videos: " + (endTime - startTime) + "ms");
        logger.info("[***CAMERA1***]Number of videos saved: " + videoCounter.get());
    }



    private List<CameraVo> getFilteredCameras(List<CameraVo> cameras) {
        //摄像头要enable
        List<CameraVo> enabledCameras = cameras.stream()
                .filter(camera -> !camera.getDisabled())
                .collect(Collectors.toList());

        //摄像头要在纽约市境内
        List<CameraVo> okCameras =new ArrayList<>();
        GeometryFactory geometryFactory=new GeometryFactory();
        for (CameraVo camera : enabledCameras) {
            Point testPoint = geometryFactory.createPoint(new Coordinate(camera.getLongitude(), camera.getLatitude()));
            boolean isInNYC = nycPolygon.contains(testPoint);
            if(isInNYC){
                okCameras.add(camera);
            }
        }
        return okCameras;
    }

    public void getRawData() {
        String urlString = camerasUrl+"?key={"+apiKey+"}&format={json}";
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
}
