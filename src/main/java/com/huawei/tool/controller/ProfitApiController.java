package com.huawei.tool.controller;

import com.huawei.tool.dao.model.CostRow;
import com.huawei.tool.dao.model.OrderDetailPageItem;
import com.huawei.tool.dao.model.TaskRow;
import com.huawei.tool.service.CostExcelImportService;
import com.huawei.tool.service.CostExcelImportService.CostImportResult;
import com.huawei.tool.service.OrderDetailManageService;
import com.huawei.tool.service.ProductCostManageService;
import com.huawei.tool.service.SummaryReportManageService;
import com.huawei.tool.service.TaskManageService;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProfitApiController {

    private final TaskManageService taskManageService;
    private final OrderDetailManageService orderDetailManageService;
    private final SummaryReportManageService summaryReportManageService;
    private final CostExcelImportService costExcelImportService;
    private final ProductCostManageService productCostManageService;

    public ProfitApiController(
            TaskManageService taskManageService,
            OrderDetailManageService orderDetailManageService,
            SummaryReportManageService summaryReportManageService,
            CostExcelImportService costExcelImportService,
            ProductCostManageService productCostManageService) {
        this.taskManageService = taskManageService;
        this.orderDetailManageService = orderDetailManageService;
        this.summaryReportManageService = summaryReportManageService;
        this.costExcelImportService = costExcelImportService;
        this.productCostManageService = productCostManageService;
    }

    @PostMapping("/tasks/upload")
    public Map<String, Object> uploadTask(@RequestParam("file") MultipartFile file) throws Exception {
        return taskManageService.uploadOrderExcel(file);
    }

    @GetMapping("/tasks")
    public List<TaskRow> listTasks() {
        return taskManageService.listTasks();
    }

    @GetMapping("/tasks/{taskId}")
    public TaskRow getTask(@PathVariable String taskId) {
        return taskManageService.getTask(taskId);
    }

    @GetMapping("/tasks/{taskId}/status")
    public Map<String, Object> status(@PathVariable String taskId) {
        return taskManageService.getTaskStatus(taskId);
    }

    @DeleteMapping("/tasks/{taskId}")
    public void deleteTask(@PathVariable String taskId) {
        taskManageService.deleteTask(taskId);
    }

    @GetMapping("/tasks/{taskId}/details")
    public Map<String, Object> detailPage(
            @PathVariable String taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String columnFilters) {
        return orderDetailManageService.pageDetails(taskId, page, size, keyword, columnFilters);
    }

    @GetMapping("/tasks/{taskId}/details/column-options")
    public List<String> detailColumnOptions(
            @PathVariable String taskId,
            @RequestParam String field,
            @RequestParam(required = false) String columnFilters) {
        return orderDetailManageService.listColumnFilterOptions(taskId, field, columnFilters);
    }

    @PutMapping("/tasks/{taskId}/details/{detailId}")
    public void updateDetail(
            @PathVariable String taskId,
            @PathVariable String detailId,
            @RequestBody OrderDetailPageItem body) {
        orderDetailManageService.updateDetail(taskId, detailId, body);
    }

    @GetMapping("/summary")
    public Map<String, Object> summaryPageAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return summaryReportManageService.pageSummaryAll(page, size);
    }

    @GetMapping("/tasks/{taskId}/summary")
    public Map<String, Object> summaryPage(
            @PathVariable String taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return summaryReportManageService.pageSummary(taskId, page, size);
    }

    @GetMapping("/tasks/{taskId}/summary/export")
    public ResponseEntity<Resource> exportSummary(@PathVariable String taskId) throws Exception {
        Path xlsx = summaryReportManageService.exportSummaryFile(taskId);
        Resource resource = new FileSystemResource(xlsx.toFile());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=summary-" + taskId + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping("/product-costs")
    public Map<String, Object> listProductCosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productCostManageService.pageProductCosts(page, size);
    }

    @PutMapping("/product-costs")
    public void updateProductCost(@RequestBody CostRow body) {
        productCostManageService.saveCost(body);
    }

    @PostMapping("/product-costs/upload")
    public CostImportResult uploadCosts(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        Path tmp = Files.createTempFile("cost-upload-", ".xlsx");
        try {
            file.transferTo(tmp.toFile());
            return costExcelImportService.importFromPath(tmp);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
