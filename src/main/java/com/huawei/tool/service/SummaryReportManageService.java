package com.huawei.tool.service;

import com.huawei.tool.constant.TaskStatus;
import com.huawei.tool.dao.SummaryReportDao;
import com.huawei.tool.dao.TaskDao;
import com.huawei.tool.dao.model.SummaryRow;
import com.huawei.tool.dao.model.TaskRow;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SummaryReportManageService {

    private final TaskDao taskDao;
    private final SummaryReportDao summaryReportDao;
    private final SummaryExportService summaryExportService;

    public SummaryReportManageService(
            TaskDao taskDao,
            SummaryReportDao summaryReportDao,
            SummaryExportService summaryExportService) {
        this.taskDao = taskDao;
        this.summaryReportDao = summaryReportDao;
        this.summaryExportService = summaryExportService;
    }

    public Map<String, Object> pageSummary(String taskId, int page, int size) {
        TaskRow t = taskDao.find(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        if (!TaskStatus.SUCCESS.equals(t.getTaskStatus())) {
            throw new IllegalArgumentException("任务未完成汇算，无法查询汇总");
        }
        int safeSize = Math.min(Math.max(size, 1), 500);
        long total = summaryReportDao.countByTask(taskId);
        int offset = Math.max(page, 0) * safeSize;
        List<SummaryRow> items = summaryReportDao.findPage(taskId, offset, safeSize);
        Map<String, Object> m = new HashMap<>();
        m.put("total", total);
        m.put("items", items);
        return m;
    }

    /** 分页返回库中全部汇算结果行（跨任务），含任务名称（便于前端展示） */
    public Map<String, Object> pageSummaryAll(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 500);
        long total = summaryReportDao.countAll();
        int offset = Math.max(page, 0) * safeSize;
        List<SummaryRow> items = summaryReportDao.findPageAll(offset, safeSize);
        Map<String, Object> m = new HashMap<>();
        m.put("total", total);
        m.put("items", items);
        return m;
    }

    public Path exportSummaryFile(String taskId) throws Exception {
        TaskRow t = taskDao.find(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        if (!TaskStatus.SUCCESS.equals(t.getTaskStatus())) {
            throw new IllegalArgumentException("任务未完成汇算，无法导出");
        }
        return summaryExportService.exportTaskSummary(taskId);
    }
}
