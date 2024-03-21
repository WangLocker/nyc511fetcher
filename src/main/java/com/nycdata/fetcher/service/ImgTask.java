package com.nycdata.fetcher.service;


import com.nycdata.fetcher.dao.CameraDao;
import com.nycdata.fetcher.entity.CameraEntity;
import com.nycdata.fetcher.vo.CameraVo;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;

public class ImgTask implements Runnable {
    private CameraVo camera;
    private String imageUrl;
    private String destinationFile;
    private CameraDao cameraDao;
    private Logger logger;
    private AtomicInteger imageCounter;

    public ImgTask(CameraVo ca, String imageUrl, String destinationFile, CameraDao cameraDao, Logger logger, AtomicInteger imageCounter) {
        this.camera = ca;
        this.imageUrl = imageUrl;
        this.destinationFile = destinationFile;
        this.cameraDao = cameraDao;
        this.logger = logger;
        this.imageCounter = imageCounter;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(3000); // 设置连接超时
            connection.setReadTimeout(3000); // 设置读取超时

            try (InputStream in = connection.getInputStream()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }

                byte[] imageData = baos.toByteArray();
                if (imageData.length > 30 * 1024) {
                    Files.write(Paths.get(destinationFile), imageData, StandardOpenOption.CREATE);
                    cameraDao.saveOneCamera(constructNewCamera(camera,0,destinationFile));
                    imageCounter.incrementAndGet();
                }
            }
        } catch (Exception e) {
            logger.error("[***CAMERA0***]Error in saving image for camera " + camera.getID(), e);
        }
    }

    private CameraEntity constructNewCamera(CameraVo ca, int type, String path) {
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
