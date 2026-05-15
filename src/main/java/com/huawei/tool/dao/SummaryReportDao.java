package com.huawei.tool.dao;

import com.huawei.tool.dao.model.SummaryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SummaryReportDao {

    void deleteByTask(@Param("taskId") String taskId);

    void batchInsert(@Param("rows") List<SummaryRow> rows);

    List<SummaryRow> findPage(@Param("taskId") String taskId, @Param("offset") int offset, @Param("limit") int limit);

    long countByTask(@Param("taskId") String taskId);

    long countAll();

    List<SummaryRow> findPageAll(@Param("offset") int offset, @Param("limit") int limit);

    List<SummaryRow> findAllByTask(@Param("taskId") String taskId);
}
