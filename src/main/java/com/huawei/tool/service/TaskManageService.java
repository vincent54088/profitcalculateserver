package com.huawei.tool.service;

import com.huawei.tool.config.AppProperties;
import com.huawei.tool.constant.TaskStatus;
import com.huawei.tool.dao.TaskDao;
import com.huawei.tool.dao.model.TaskRow;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskManageService {

    private final AppProperties appProperties;
    private final TaskDao taskDao;
    private final TaskApplicationService taskApplicationService;

    public TaskManageService(
            AppProperties appProperties,
            TaskDao taskDao,
            TaskApplicationService taskApplicationService) {
        this.appProperties = appProperties;
        this.taskDao = taskDao;
        this.taskApplicationService = taskApplicationService;
    }

    public Map<String, Object> uploadOrderExcel(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        String original = file.getOriginalFilename();
        if (original == null || (!original.toLowerCase().endsWith(".xlsx") && !original.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("仅支持 .xlsx / .xls");
        }
        String taskId = UUID.randomUUID().toString().replace("-", "");
        Path dir = Paths.get(appProperties.getUploadDir(), taskId);
        Files.createDirectories(dir);
        String safeName = Paths.get(original).getFileName().toString().replace("..", "_");
        Path dest = dir.resolve(safeName);
        file.transferTo(dest.toFile());

        int dot = safeName.lastIndexOf('.');
        String taskName = null;
        if (dot > 0) {
            taskName = safeName.substring(0, dot);
        } else {
            taskName = safeName;
        }
        taskDao.insert(taskId, taskName, TaskStatus.PARSING, safeName);
        taskApplicationService.processOrderUploadAsync(taskId, dest);

        Map<String, Object> m = new HashMap<>();
        m.put("taskId", taskId);
        m.put("taskName", taskName);
        m.put("taskStatus", TaskStatus.PARSING);
        return m;
    }

    public List<TaskRow> listTasks() {
        return taskDao.listAll();
    }

    public TaskRow getTask(String taskId) {
        return taskDao.find(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在"));
    }

    public Map<String, Object> getTaskStatus(String taskId) {
        TaskRow t = taskDao.find(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        Map<String, Object> m = new HashMap<>();
        m.put("taskId", t.getTaskId());
        m.put("taskStatus", t.getTaskStatus());
        m.put("failReason", t.getFailReason());
        return m;
    }

    public void deleteTask(String taskId) {
        taskDao.find(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        Path dir = Paths.get(appProperties.getUploadDir(), taskId);
        taskDao.delete(taskId);
        try {
            Files.walk(dir).sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }
}
