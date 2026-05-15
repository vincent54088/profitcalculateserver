package com.huawei.tool.service;

import com.huawei.tool.dao.OrderDetailDao;
import com.huawei.tool.dao.OrderInfoDao;
import com.huawei.tool.dao.TaskDao;
import com.huawei.tool.dao.model.DetailRow;
import com.huawei.tool.dao.model.OrderDetailPageItem;
import com.huawei.tool.dao.model.OrderRow;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrderDetailManageService {

    private static final Set<String> DETAIL_FILTER_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "orderId",
            "area",
            "representativeOffice",
            "country",
            "accounts",
            "project",
            "poId",
            "grossProfit",
            "deviceType",
            "deviceId",
            "description",
            "categoryType",
            "hardwareType",
            "scene",
            "deviceCount",
            "incomeMonth",
            "currency",
            "listPrice",
            "beforeTier",
            "beforePrice",
            "discountDescBefore",
            "afterTier",
            "afterPrice",
            "discountDescAfter",
            "tierIncrease",
            "priceIncrease",
            "afterTierDiscountRate",
            "afterPriceDiscountRate",
            "beforeTotalPrice",
            "afterTotalPrice",
            "totalPriceIncrease",
            "totalIncreaseRate",
            "additionInfo")));

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TaskDao taskDao;
    private final OrderDetailDao orderDetailDao;
    private final OrderInfoDao orderInfoDao;
    private final TaskApplicationService taskApplicationService;

    public OrderDetailManageService(
            TaskDao taskDao,
            OrderDetailDao orderDetailDao,
            OrderInfoDao orderInfoDao,
            TaskApplicationService taskApplicationService) {
        this.taskDao = taskDao;
        this.orderDetailDao = orderDetailDao;
        this.orderInfoDao = orderInfoDao;
        this.taskApplicationService = taskApplicationService;
    }

    public Map<String, Object> pageDetails(String taskId, int page, int size, String keyword, String columnFiltersJson) {
        taskDao.find(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        int safeSize = Math.min(Math.max(size, 1), 500);
        int offset = Math.max(page, 0) * safeSize;
        Map<String, Object> m = new HashMap<>();
        Map<String, List<String>> columnFilters = parseColumnFilters(columnFiltersJson);
        String kw = keyword == null ? null : keyword.trim();
        if (kw != null && kw.isEmpty()) {
            kw = null;
        }
        if (columnFilters != null && !columnFilters.isEmpty()) {
            kw = null;
        }
        long total = orderDetailDao.countByTask(taskId, kw, columnFilters);
        List<OrderDetailPageItem> items = orderDetailDao.findPage(taskId, kw, columnFilters, offset, safeSize);
        m.put("detailKind", "ORDER");
        m.put("total", total);
        m.put("items", items);
        return m;
    }

    public void updateDetail(String taskId, String detailId, OrderDetailPageItem body) {
        taskDao.find(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        DetailRow existing = orderDetailDao.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("明细不存在"));
        if (!taskId.equals(existing.getTaskId())) {
            throw new IllegalArgumentException("任务与明细不匹配");
        }
        DetailRow merged = DetailMerge.merge(existing, body);
        orderDetailDao.updateRow(merged);

        orderInfoDao.findByTaskAndOrder(taskId, existing.getOrderId()).ifPresent(oe -> {
            OrderRow patch = orderPatchFrom(body);
            OrderMerge.merge(oe, patch);
            orderInfoDao.updateOrder(taskId, oe);
        });

        taskApplicationService.triggerRecalculateAsync(taskId);
    }

    /**
     * 列筛选可选值：在「其它列」已选条件下，该列在任务数据中的去重值（不含本列筛选，便于多选切换）。
     */
    public List<String> listColumnFilterOptions(String taskId, String field, String columnFiltersJson) {
        taskDao.find(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        if (!DETAIL_FILTER_KEYS.contains(field)) {
            throw new IllegalArgumentException("无效列");
        }
        Map<String, List<String>> filters = parseColumnFilters(columnFiltersJson);
        if (filters != null) {
            filters.remove(field);
            if (filters.isEmpty()) {
                filters = null;
            }
        }
        return orderDetailDao.listDistinctColumnValues(taskId, field, filters);
    }

    private static Map<String, List<String>> parseColumnFilters(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> raw = OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
            Map<String, List<String>> out = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : raw.entrySet()) {
                if (!DETAIL_FILTER_KEYS.contains(e.getKey())) {
                    continue;
                }
                List<String> vals = normalizeFilterValues(e.getValue());
                if (!vals.isEmpty()) {
                    out.put(e.getKey(), vals);
                }
            }
            return out.isEmpty() ? null : out;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static List<String> normalizeFilterValues(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List<?>) {
            List<String> r = new ArrayList<>();
            for (Object o : (List<?>) value) {
                if (o == null) {
                    continue;
                }
                r.add(String.valueOf(o));
            }
            return r;
        }
        return Collections.singletonList(String.valueOf(value));
    }

    private static OrderRow orderPatchFrom(OrderDetailPageItem b) {
        OrderRow p = new OrderRow();
        p.setOrderId(b.getOrderId());
        p.setArea(b.getArea());
        p.setRepresentativeOffice(b.getRepresentativeOffice());
        p.setCountry(b.getCountry());
        p.setAccounts(b.getAccounts());
        p.setProject(b.getProject());
        p.setPoId(b.getPoId());
        return p;
    }
}
