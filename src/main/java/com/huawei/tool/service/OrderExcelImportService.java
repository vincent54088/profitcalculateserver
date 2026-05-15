package com.huawei.tool.service;

import com.huawei.tool.config.ExcelMappingProperties;
import com.huawei.tool.dao.OrderDetailDao;
import com.huawei.tool.dao.OrderInfoDao;
import com.huawei.tool.dao.model.DetailRow;
import com.huawei.tool.dao.model.OrderRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderExcelImportService {

    private static final DataFormatter FORMATTER = new DataFormatter();
    /** 单次导入内对公式单元格求值，与 {@link CostExcelImportService} 行为一致。 */
    private static final ThreadLocal<FormulaEvaluator> CELL_EVAL = new ThreadLocal<>();

    private final ExcelMappingProperties mappingProperties;
    private final OrderInfoDao orderInfoDao;
    private final OrderDetailDao orderDetailDao;

    public OrderExcelImportService(
            ExcelMappingProperties mappingProperties,
            OrderInfoDao orderInfoDao,
            OrderDetailDao orderDetailDao) {
        this.mappingProperties = mappingProperties;
        this.orderInfoDao = orderInfoDao;
        this.orderDetailDao = orderDetailDao;
    }

    @Transactional
    public void importFromPath(String taskId, Path path) throws Exception {
        try (InputStream in = Files.newInputStream(path);
                Workbook wb = WorkbookFactory.create(in)) {
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            CELL_EVAL.set(evaluator);
            try {
                List<String> oneSheetNames = mappingProperties.getOrder().getOrderDataSheetNames();
                if (oneSheetNames == null || oneSheetNames.isEmpty()) {
                    throw new IllegalArgumentException(
                            "未配置订单 Sheet 名：请在 excel.order.order-data-sheet-names 中至少填写一个与工作簿标签一致的名称。");
                }
                Sheet sheet = findSheet(wb, oneSheetNames);
                if (sheet == null) {
                    throw new IllegalArgumentException("未找到订单数据 Sheet，名称需为配置之一: " + oneSheetNames);
                }
                importCombinedSingleSheet(taskId, sheet);
            } finally {
                CELL_EVAL.remove();
            }
        }
    }

    /**
     * 单 Sheet：表头同时包含订单列与明细列，每行生成一条明细；同一合同号首次出现时写入订单头。
     * 固定「双行表头」：第 1 行仅作分组/合并标题（不参与列名解析），第 2 行为列名行（须含「合同号」）；
     * 数据从第 3 行起；若明细续行未填合同号（纵向合并导出），沿用上一条有效合同号。
     */
    private void importCombinedSingleSheet(String taskId, Sheet sheet) {
        List<String> errors = new ArrayList<>();
        int first = sheet.getFirstRowNum();
        Row headerRow = sheet.getRow(first + 1);
        if (headerRow == null) {
            throw new IllegalArgumentException(
                    "订单数据表须为双行表头：第 2 行为列名行，当前第 2 行为空。");
        }
        Map<String, Integer> header = headerIndex(headerRow);
        if (!header.containsKey("合同号")) {
            throw new IllegalArgumentException(
                    "订单数据表须为双行表头：第 2 行须为列名行且包含「合同号」。");
        }
        int dataStartRow = first + 2;
        augmentListPriceColumn(header);
        augmentOrderHeaderAliases(header);

        Map<String, String> orderMap = mappingProperties.getOrder().getOrderColumnMap();
        // 明细列在 parseDetailRow 中均用 colOpt，表中有则映射、无则留空；不应按 detail-column-map 全量强制表头存在。
        LinkedHashSet<String> required = new LinkedHashSet<>(orderMap.keySet());
        requireHeaders(header, required, "订单数据表");

        Map<String, OrderRow> orderById = new LinkedHashMap<>();
        List<DetailRow> detailRows = new ArrayList<>();
        String lastContractId = null;
        for (int r = dataStartRow; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null || orderDataRowIsBlank(row, header)) {
                continue;
            }
            String oidRaw = text(row, col(header, "合同号"));
            String oid;
            if (oidRaw != null && !oidRaw.trim().isEmpty()) {
                oid = oidRaw.trim();
                lastContractId = oid;
            } else if (lastContractId != null) {
                oid = lastContractId;
            } else {
                errors.add("订单数据表第" + (r + 1) + "行: 合同号为空（且上方无有效合同号可沿用）");
                continue;
            }
            if (!orderById.containsKey(oid)) {
                OrderRow o = parseOrderRow(row, header);
                o.setOrderId(oid);
                orderById.put(oid, o);
            } else {
                OrderRow patch = parseOrderRow(row, header);
                patch.setOrderId(oid);
                OrderMerge.merge(orderById.get(oid), patch);
            }
            detailRows.add(parseDetailRow(taskId, row, header, oid));
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors.subList(0, Math.min(20, errors.size()))));
        }
        if (orderById.isEmpty()) {
            throw new IllegalArgumentException("订单数据表无有效数据行");
        }
        if (detailRows.isEmpty()) {
            throw new IllegalArgumentException("订单数据表无有效明细行");
        }

        orderInfoDao.batchInsert(taskId, new ArrayList<>(orderById.values()));
        orderDetailDao.batchInsert(detailRows);
    }

    private static OrderRow parseOrderRow(Row row, Map<String, Integer> header) {
        OrderRow o = new OrderRow();
        String id = text(row, col(header, "合同号"));
        o.setOrderId(id == null ? null : id.trim());
        o.setArea(text(row, col(header, "区域")));
        o.setRepresentativeOffice(text(row, col(header, "代表处")));
        o.setCountry(text(row, col(header, "国家")));
        o.setAccounts(text(row, col(header, "客户群")));
        o.setProject(text(row, col(header, "项目")));
        o.setPoId(text(row, col(header, "PO号")));
        o.setProductDomain(text(row, col(header, "产品领域")));
        return o;
    }

    private static DetailRow parseDetailRow(String taskId, Row row, Map<String, Integer> header, String orderId) {
        DetailRow d = new DetailRow();
        d.setId(UUID.randomUUID().toString().replace("-", ""));
        d.setTaskId(taskId);
        d.setOrderId(orderId);
        d.setDeviceType(text(row, colOpt(header, "型号")));
        d.setDeviceId(text(row, colOpt(header, "编码")));
        d.setDescription(text(row, colOpt(header, "编码描述")));
        d.setCategoryType(text(row, colOpt(header, "软硬件分类")));
        d.setHardwareType(text(row, colOpt(header, "硬件类型")));
        d.setScene(text(row, colOpt(header, "使用场景")));
        d.setDeviceCount(intVal(row, colOpt(header, "数量")));
        d.setIncomeMonth(text(row, colOpt(header, "预计收入触发时间")));
        d.setCurrency(text(row, colOpt(header, "币种")));
        d.setListPrice(decimal(row, colOpt(header, "目录价")));
        d.setBeforeTier(decimal(row, colOpt(header, "提价前-价位")));
        d.setBeforePrice(decimal(row, colOpt(header, "提价前-价格")));
        d.setDiscountDescBefore(text(row, colOpt(header, "优惠说明(前)")));
        d.setAfterTier(decimal(row, colOpt(header, "提价后-价位")));
        d.setAfterPrice(decimal(row, colOpt(header, "提价后-价格")));
        d.setDiscountDescAfter(text(row, colOpt(header, "优惠说明(后)")));
        d.setTierIncrease(decimal(row, colOpt(header, "价位提价")));
        d.setPriceIncrease(decimal(row, colOpt(header, "价格提价")));
        d.setAfterTierDiscountRate(decimal(row, colOpt(header, "提价后-价位折扣率")));
        d.setAfterPriceDiscountRate(decimal(row, colOpt(header, "提价后-价格折扣率")));
        d.setBeforeTotalPrice(decimal(row, colOpt(header, "提价前-总价")));
        d.setAfterTotalPrice(decimal(row, colOpt(header, "提价后-总价")));
        d.setTotalPriceIncrease(decimal(row, colOpt(header, "总价上涨")));
        d.setTotalIncreaseRate(decimal(row, colOpt(header, "涨价%")));
        d.setGrossProfit(decimal(row, colOpt(header, "销毛")));
        d.setAdditionInfo(text(row, colOpt(header, "备注")));
        return d;
    }

    /**
     * 若表头中已有「目录价」则不变；否则将「以目录价结尾」的第一列别名为「目录价」，供后续按固定列名取值。
     */
    private static void augmentListPriceColumn(Map<String, Integer> header) {
        if (header.containsKey("目录价")) {
            return;
        }
        for (Map.Entry<String, Integer> e : header.entrySet()) {
            String name = e.getKey();
            if (name != null && name.endsWith("目录价")) {
                header.put("目录价", e.getValue());
                return;
            }
        }
    }

    /**
     * 常见导出列名与解析所用列名不一致时的别名（须在 requireHeaders 与数据行解析之前调用）。
     */
    private static void augmentOrderHeaderAliases(Map<String, Integer> header) {
        if (!header.containsKey("PO号") && header.containsKey("PO")) {
            header.put("PO号", header.get("PO"));
        }
    }

    /**
     * 配置中的订单列名与 Excel 表头须一致；PO / PO号 视为同一列（避免配置或模板一侧写 PO、另一侧写 PO号 时误报缺列）。
     */
    private static boolean headerSatisfiesRequiredName(Map<String, Integer> header, String requiredName) {
        if (requiredName == null || requiredName.isEmpty()) {
            return true;
        }
        if (header.containsKey(requiredName)) {
            return true;
        }
        if ("PO".equals(requiredName)) {
            return header.containsKey("PO号");
        }
        if ("PO号".equals(requiredName)) {
            return header.containsKey("PO");
        }
        return false;
    }

    private static void requireHeaders(Map<String, Integer> header, Collection<String> required, String which) {
        List<String> missing = new ArrayList<>();
        for (String k : required) {
            if (!headerSatisfiesRequiredName(header, k)) {
                missing.add(k);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(which + "缺少列: " + String.join(", ", missing));
        }
    }

    private static Sheet findSheet(Workbook wb, List<String> names) {
        if (names == null) {
            return null;
        }
        for (String want : names) {
            if (want == null) {
                continue;
            }
            Sheet s = wb.getSheet(want.trim());
            if (s != null) {
                return s;
            }
        }
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            String n = wb.getSheetName(i);
            for (String want : names) {
                if (want != null && want.trim().equals(n.trim())) {
                    return wb.getSheetAt(i);
                }
            }
        }
        return null;
    }

    private static Map<String, Integer> headerIndex(Row row) {
        Map<String, Integer> m = new LinkedHashMap<>();
        if (row == null) {
            return m;
        }
        for (Cell cell : row) {
            String h = cellString(cell);
            if (h != null && !h.isEmpty()) {
                m.put(h.trim(), cell.getColumnIndex());
            }
        }
        return m;
    }

    /**
     * 是否跳过该行：仅看明细关键列——编码（device_id）、提价前-总价、提价后-总价是否均无有效内容。
     */
    private static boolean orderDataRowIsBlank(Row row, Map<String, Integer> header) {
        return isBlank(text(row, colOpt(header, "编码")))
                && isBlank(text(row, colOpt(header, "提价前-总价")))
                && isBlank(text(row, colOpt(header, "提价后-总价")));
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String cellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        FormulaEvaluator ev = CELL_EVAL.get();
        String t = (ev != null ? FORMATTER.formatCellValue(cell, ev) : FORMATTER.formatCellValue(cell)).trim();
        return t.isEmpty() ? null : t;
    }

    private static String text(Row row, Integer col) {
        if (col == null || row == null) {
            return null;
        }
        return cellString(row.getCell(col));
    }

    private static BigDecimal decimal(Row row, Integer col) {
        String t = text(row, col);
        if (t == null) {
            return null;
        }
        try {
            String normalized = t.replace(",", "").replace("%", "").trim();
            if (normalized.isEmpty()) {
                return null;
            }
            return new BigDecimal(normalized);
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer intVal(Row row, Integer col) {
        String t = text(row, col);
        if (t == null) {
            return null;
        }
        try {
            return Integer.parseInt(t.replace(",", "").trim().split("\\.")[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private static int col(Map<String, Integer> header, String name) {
        Integer i = header.get(name);
        if (i == null) {
            throw new IllegalArgumentException("缺少列: " + name);
        }
        return i;
    }

    private static Integer colOpt(Map<String, Integer> header, String name) {
        return header.get(name);
    }
}
