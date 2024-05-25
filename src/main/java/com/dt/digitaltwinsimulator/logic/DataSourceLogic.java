package com.dt.digitaltwinsimulator.logic;

import com.dt.digitaltwinsimulator.entity.DBConnectionEntity;
import com.dt.digitaltwinsimulator.repository.DBConnectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DataSourceLogic {

    private final DBConnectionRepository dbConnectionRepository;

    public DataSourceLogic(DBConnectionRepository dbConnectionRepository) {
        this.dbConnectionRepository = dbConnectionRepository;
    }

    public String getTableList() {
        log.info("getTableList");

        List<DBConnectionEntity> dbConnectionEntityList = dbConnectionRepository.findAll();

        for (DBConnectionEntity dbConnectionEntity : dbConnectionEntityList) {
            log.info("dbConnectionEntity: {}", dbConnectionEntity);
        }

        return null;
    }

    public void testConnection() {
        log.info("testConnection");

        long test = dbConnectionRepository.count();
    }
}
