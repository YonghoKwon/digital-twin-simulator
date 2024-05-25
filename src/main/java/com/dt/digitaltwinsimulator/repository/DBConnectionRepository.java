package com.dt.digitaltwinsimulator.repository;

import com.dt.digitaltwinsimulator.entity.DBConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DBConnectionRepository extends JpaRepository<DBConnectionEntity, Long> {
    // 간단한 쿼리를 위한 메서드
    long count();
}
