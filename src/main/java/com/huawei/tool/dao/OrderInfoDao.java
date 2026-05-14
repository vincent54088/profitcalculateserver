package com.huawei.tool.dao;

import com.huawei.tool.dao.model.OrderRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface OrderInfoDao {

    List<OrderRow> selectByTaskId(@Param("taskId") String taskId);

    OrderRow selectByTaskIdAndOrderId(@Param("taskId") String taskId, @Param("orderId") String orderId);

    void updateOrder(@Param("taskId") String taskId, @Param("r") OrderRow r);

    void batchInsert(@Param("taskId") String taskId, @Param("rows") List<OrderRow> rows);

    default Optional<OrderRow> findByTaskAndOrder(String taskId, String orderId) {
        OrderRow row = selectByTaskIdAndOrderId(taskId, orderId);
        return row == null ? Optional.empty() : Optional.of(row);
    }

    default Map<String, OrderRow> mapByOrderId(String taskId) {
        List<OrderRow> rows = selectByTaskId(taskId);
        Map<String, OrderRow> m = new HashMap<>();
        for (OrderRow r : rows) {
            m.put(r.getOrderId(), r);
        }
        return m;
    }
}
