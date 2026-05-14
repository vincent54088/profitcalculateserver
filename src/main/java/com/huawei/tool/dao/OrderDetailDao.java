package com.huawei.tool.dao;

import com.huawei.tool.dao.model.DetailRow;
import com.huawei.tool.dao.model.OrderDetailPageItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface OrderDetailDao {

    void batchInsert(@Param("rows") List<DetailRow> rows);

    List<OrderDetailPageItem> findPage(
            @Param("taskId") String taskId,
            @Param("keyword") String keyword,
            @Param("columnFilters") Map<String, List<String>> columnFilters,
            @Param("offset") int offset,
            @Param("limit") int limit);

    long countByTask(
            @Param("taskId") String taskId,
            @Param("keyword") String keyword,
            @Param("columnFilters") Map<String, List<String>> columnFilters);

    List<String> listDistinctColumnValues(
            @Param("taskId") String taskId,
            @Param("field") String field,
            @Param("columnFilters") Map<String, List<String>> columnFilters);

    DetailRow findRowById(@Param("id") String id);

    void updateRow(DetailRow r);

    List<DetailRow> findAllByTask(@Param("taskId") String taskId);

    default Optional<DetailRow> findById(String id) {
        DetailRow r = findRowById(id);
        return r == null ? Optional.empty() : Optional.of(r);
    }
}
