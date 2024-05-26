package com.dt.digitaltwinsimulator.controller;

import com.dt.digitaltwinsimulator.entity.dto.DBConnectionInfoDto;
import com.dt.digitaltwinsimulator.logic.DataSourceLogic;
import com.dt.digitaltwinsimulator.logic.DynamicDataSourceLogic;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "DB Dynamic Connection Controller")
@RestController
@RequestMapping("/api/db")
class DBConnectionController {

    private final DynamicDataSourceLogic dynamicDataSourceLogic;
    private final DataSourceLogic dataSourceLogic;

    public DBConnectionController(
            DynamicDataSourceLogic dynamicDataSourceLogic,
            DataSourceLogic dataSourceLogic
    ) {
        this.dynamicDataSourceLogic = dynamicDataSourceLogic;
        this.dataSourceLogic = dataSourceLogic;
    }

    @ApiOperation(value = "", notes = "DB Connection")
    @PostMapping("/connect/{db}")
    public ResponseEntity<String> connectToDatabase(
            @PathVariable String db,
            @RequestBody DBConnectionInfoDto dbConnectionInfoDto
    ) {
        boolean success = dynamicDataSourceLogic.setDataSource(db, dbConnectionInfoDto);
        return success
                ? ResponseEntity.ok("Connected to database")
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to database");
    }

    @GetMapping("/test-connection")
    public ResponseEntity<String> testConnection() {
        boolean success = dynamicDataSourceLogic.testConnection();

        return success ? ResponseEntity.ok("Connection is valid") : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to database");
    }

    // db table 조회
    @ApiOperation(value = "", notes = "DB Table 조회")
    @GetMapping("/table")
    public ResponseEntity<String> getTableList(
    ) {
        return ResponseEntity.ok(dataSourceLogic.getTableList());
    }
}
