package com.huawei.tool.dao;

import com.huawei.tool.constant.TaskStatus;
import com.huawei.tool.dao.model.TaskRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TaskDao {

    void insert(
            @Param("taskId") String taskId,
            @Param("taskName") String taskName,
            @Param("status") String status,
            @Param("fileName") String fileName);

    void updateStatus(@Param("taskId") String taskId, @Param("status") String status, @Param("failReason") String failReason);

    void markSuccessWithStatus(@Param("taskId") String taskId, @Param("status") String status);

    void markFailedWithStatus(@Param("taskId") String taskId, @Param("status") String status, @Param("reason") String reason);

    void delete(@Param("taskId") String taskId);

    TaskRow findRow(@Param("taskId") String taskId);

    List<TaskRow> selectAllOrderByCreateTimeDesc();

    default Optional<TaskRow> find(String taskId) {
        TaskRow r = findRow(taskId);
        return r == null ? Optional.empty() : Optional.of(r);
    }

    default void markSuccess(String taskId) {
        markSuccessWithStatus(taskId, TaskStatus.SUCCESS);
    }

    default void markFailed(String taskId, String reason) {
        markFailedWithStatus(taskId, TaskStatus.FAILED, reason);
    }

    default List<TaskRow> listAll() {
        return selectAllOrderByCreateTimeDesc();
    }
}
