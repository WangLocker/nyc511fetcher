package com.nycdata.fetcher.dao;

import com.nycdata.fetcher.entity.CameraEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CameraDao {
    @Autowired
    private CameraRepository cameraRepository;

    public void saveOneCamera(CameraEntity toInsert) {
        cameraRepository.save(toInsert);
    }
}
