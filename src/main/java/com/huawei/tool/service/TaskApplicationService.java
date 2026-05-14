package com.huawei.tool.service;

import com.huawei.tool.config.AsyncConfig;
import com.huawei.tool.constant.TaskStatus;
import com.huawei.tool.dao.TaskDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class TaskApplicationService {

    private static final Logger log = LoggerFactory.getLogger(TaskApplicationService.class);

    private final TaskDao taskDao;
    private final OrderExcelImportService orderExcelImportService;
    private final ProfitCalculationService profitCalculationService;

    public TaskApplicationService(
            TaskDao taskDao,
            OrderExcelImportService orderExcelImportService,
            ProfitCalculationService profitCalculationService) {
        this.taskDao = taskDao;
        this.orderExcelImportService = orderExcelImportService;
        this.profitCalculationService = profitCalculationService;
    }

    @Async(AsyncConfig.TASK_EXECUTOR)
    public void processOrderUploadAsync(String taskId, Path savedFile) {
        try {
            taskDao.updateStatus(taskId, TaskStatus.PARSING, null);
            orderExcelImportService.importFromPath(taskId, savedFile);
            taskDao.updateStatus(taskId, TaskStatus.CALCULATING, null);
            profitCalculationService.recalculate(taskId);
            taskDao.markSuccess(taskId);
        } catch (Exception ex) {
            log.error("任务处理失败 taskId={}", taskId, ex);
            String msg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            if (msg.length() > 2000) {
                msg = msg.substring(0, 2000);
            }
            taskDao.markFailed(taskId, msg);
        }
    }

    @Async(AsyncConfig.TASK_EXECUTOR)
    public void triggerRecalculateAsync(String taskId) {
        try {
            taskDao.updateStatus(taskId, TaskStatus.CALCULATING, null);
            profitCalculationService.recalculate(taskId);
            taskDao.markSuccess(taskId);
        } catch (Exception ex) {
            log.error("重算失败 taskId={}", taskId, ex);
            String msg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            taskDao.markFailed(taskId, msg);
        }
    }
}
