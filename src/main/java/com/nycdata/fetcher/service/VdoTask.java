package com.nycdata.fetcher.service;


import com.nycdata.fetcher.dao.CameraDao;
import com.nycdata.fetcher.entity.CameraEntity;
import com.nycdata.fetcher.vo.CameraVo;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class VdoTask implements Runnable {
    private CameraVo camera;
    private String videoUrl;
    private String destinationFile;
    private CameraDao cameraDao;
    private Logger logger;
    private AtomicInteger videoCounter;

    public VdoTask(CameraVo ca, String videoUrl, String destinationFile, CameraDao cameraDao, Logger logger, AtomicInteger videoCounter) {
        this.camera = ca;
        this.videoUrl = videoUrl;
        this.destinationFile = destinationFile;
        this.cameraDao = cameraDao;
        this.logger = logger;
        this.videoCounter = videoCounter;
    }

    @Override
    public void run() {
        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg", "-i", videoUrl, "-ss", "00:00:00", "-t", "00:00:30", "-c", "copy", destinationFile
        );

        builder.redirectErrorStream(true); // 合并标准输出和错误输出
        try {
            Process process = builder.start();
            // 输出ffmpeg的输出信息
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // 等待Process完成，设置超时时间为60秒
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                // 超时逻辑处理
                logger.warn("[***CAMERA1***]ffmpeg process timeout."+camera.getID());
                process.destroy(); // 杀死Process
                return;
            }

        } catch (Exception e) {
            logger.error("[***CAMERA1***]Error in saving video for camera "+camera.getID());
        }

        if(Files.exists(Paths.get(destinationFile))){
            cameraDao.saveOneCamera(constructNewCamera(camera,1,destinationFile));
            videoCounter.incrementAndGet();
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
