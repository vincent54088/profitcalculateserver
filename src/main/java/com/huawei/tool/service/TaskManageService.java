package com.huawei.tool.service;

import com.huawei.tool.config.AppProperties;
import com.huawei.tool.constant.TaskStatus;
import com.huawei.tool.dao.TaskDao;
import com.huawei.tool.dao.model.TaskRow;
import com.huawei.tool.util.UploadFilenameHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
        String original = UploadFilenameHelper.decodeOriginalFilename(file.getOriginalFilename());
        if (original == null || (!original.toLowerCase().endsWith(".xlsx") && !original.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("仅支持 .xlsx / .xls");
        }
        String taskId = UUID.randomUUID().toString().replace("-", "");
        Path dir = Paths.get(appProperties.getUploadDir(), taskId);
        Files.createDirectories(dir);
        String displayName = UploadFilenameHelper.displayFileName(original);
        String storageName = UploadFilenameHelper.storageFileName(original);
        Path dest = dir.resolve(storageName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        String taskName = UploadFilenameHelper.taskNameFromFile(displayName);
        taskDao.insert(taskId, taskName, TaskStatus.PARSING, displayName);
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
