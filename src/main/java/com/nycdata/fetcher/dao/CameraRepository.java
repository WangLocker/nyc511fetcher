package com.nycdata.fetcher.dao;

import com.nycdata.fetcher.entity.CameraEntity;
import com.nycdata.fetcher.entity.EventKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CameraRepository extends JpaRepository<CameraEntity, EventKey> {
}
