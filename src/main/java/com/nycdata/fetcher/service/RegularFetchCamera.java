package com.nycdata.fetcher.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nycdata.fetcher.dao.CameraDao;

import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

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

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    @Scheduled(fixedRate = 600000) // 每300秒执行一次
    public void performTask1AtFixedRate() {
        logger.info("[***CAMERA0***]Fetching cameras from " + camerasUrl);
        getRawData();
        Gson gson = new Gson();
        Type eventType = new TypeToken<List<CameraVo>>(){}.getType();
        List<CameraVo> cameras = gson.fromJson(rawData, eventType);

        //过滤摄像头
        List<CameraVo> okCameras=getFilteredCameras(cameras);
        logger.info("[***CAMERA0***]Number of enabled cameras in NYC: "+okCameras.size());

        // 初始化图片和视频计数器
        int imageCounter = 0;
        int dropImageCounter = 0;

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        //对于每个enabled且在纽约市境内的摄像头，若url不为null保存其图片，若videourl不为null保存其视频
        for (CameraVo camera : okCameras) {
            if (camera.getUrl() != null) {
                //图片URL与图片保存路径
                String imageUrl = camera.getUrl();
                String destinationFile = imgPath + "/" +camera.getID().replace("-","_") +"_"+System.currentTimeMillis()/1000+ ".jpg";

                //读取图片，判断大小，过滤掉质量太低的图片
                try {
                    URL url = new URL(imageUrl);
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(2000); // 设置连接超时为3秒（3000毫秒）
                    connection.setReadTimeout(2000); // 设置读取超时为3秒（3000毫秒）

                    try(InputStream in = connection.getInputStream()){
                        // 使用ByteArrayOutputStream来捕获数据
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        // 从InputStream中读取数据
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }

                        // 转换为byte数组，检查数据大小
                        byte[] imageData = baos.toByteArray();
                        if (imageData.length < 30 * 1024) {
                            dropImageCounter++;
                        } else {
                            // 将数据写入文件
                            Files.write(Paths.get(destinationFile), imageData, StandardOpenOption.CREATE);
                            cameraDao.saveOneCamera(constructNewCamera(camera,0,destinationFile));
                            imageCounter++;
                        }
                    }catch (Exception e){
                        logger.error("[***CAMERA0***]Error in saving image for camera "+camera.getID());
                    }

                } catch (MalformedURLException e) {
                    logger.error("MalformedURLException e "+camera.getID());
                } catch (IOException e) {
                    logger.error("IOException e "+camera.getID());
                }
            }
        }

        long endTime = System.currentTimeMillis();
        logger.info("[***CAMERA0***]Time taken to fetch and save images and videos: " + (endTime - startTime) + "ms");
        logger.info("[***CAMERA0***]Number of images saved: " + imageCounter);
        logger.info("[***CAMERA0***]Number of images dropped: " + dropImageCounter);
    }


    @Scheduled(fixedRate = 600000) // 每600秒执行一次
    public void performTask2AtFixedRate() {
        logger.info("[***CAMERA1***]Fetching cameras from " + camerasUrl);
        getRawData();
        Gson gson = new Gson();
        Type eventType = new TypeToken<List<CameraVo>>(){}.getType();
        List<CameraVo> cameras = gson.fromJson(rawData, eventType);

        //过滤摄像头
        List<CameraVo> okCameras=getFilteredCameras(cameras);
        logger.info("[***CAMERA1***]Number of enabled cameras in NYC: "+okCameras.size());

        // 初始化图片和视频计数器
        int videoCounter = 0;

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        //对于每个enabled且在纽约市境内的摄像头，若url不为null保存其图片，若videourl不为null保存其视频
        for (CameraVo camera : okCameras) {
            if (camera.getVideoUrl() != null) {
                //视频URL与视频保存路径
                String videoUrl = camera.getVideoUrl();
                String destinationFile = vdoPath + "/"+camera.getID().replace("-","_") +"_"+System.currentTimeMillis()/1000+ ".mp4";

                ProcessBuilder builder = new ProcessBuilder(
                        "ffmpeg", "-i", videoUrl, "-ss", "00:00:00", "-t", "00:00:30", "-c", "copy", destinationFile
                );

                builder.redirectErrorStream(true); // 合并标准输出和错误输出
                try {
                    Process process = builder.start();

                    // 输出ffmpeg的输出信息
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    // 等待Process完成，设置超时时间为60秒
                    boolean finished = process.waitFor(90, TimeUnit.SECONDS);
                    if (!finished) {
                        // 超时逻辑处理
                        logger.warn("[***CAMERA1***]ffmpeg process timeout."+camera.getID());
                        process.destroy(); // 杀死Process
                        continue;
                    }

                } catch (Exception e) {
                    logger.error("[***CAMERA1***]Error in saving video for camera "+camera.getID());
                }

                if(Files.exists(Paths.get(destinationFile))){
                    cameraDao.saveOneCamera(constructNewCamera(camera,1,destinationFile));
                    videoCounter++;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        logger.info("[***CAMERA1***]Time taken to fetch and save images and videos: " + (endTime - startTime) + "ms");
        logger.info("[***CAMERA1***]Number of videos saved: " + videoCounter);
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

    private CameraEntity constructNewCamera(CameraVo ca,int type,String path) {
        CameraEntity ca1= new CameraEntity();
        ca1.setID_LU(ca.getID(),System.currentTimeMillis());
        ca1.setName(ca.getName());
        ca1.setLatitude(ca.getLatitude());
        ca1.setLongitude(ca.getLongitude());
        ca1.setRoadwayName(ca.getRoadwayName());
        ca1.setDirectionOfTravel(ca.getDirectionOfTravel());
        ca1.setType(type);
        ca1.setPath(path);
        return ca1;
    }
}
