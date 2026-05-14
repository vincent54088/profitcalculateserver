package com.huawei.tool.service;

import com.huawei.tool.config.ExcelMappingProperties;
import com.huawei.tool.dao.ProductCostDao;
import com.huawei.tool.dao.model.CostRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CostExcelImportService {

    private static final DataFormatter FORMATTER = new DataFormatter();
    private static final Pattern MONTH_HEADER = Pattern.compile("^\\s*(\\d{1,2})\\s*月\\s*$");

    private final ExcelMappingProperties mappingProperties;
    private final ProductCostDao productCostDao;

    public CostExcelImportService(ExcelMappingProperties mappingProperties, ProductCostDao productCostDao) {
        this.mappingProperties = mappingProperties;
        this.productCostDao = productCostDao;
    }

    public CostImportResult importFromPath(Path path) throws Exception {
        CostImportResult result = new CostImportResult();
        try (InputStream in = Files.newInputStream(path);
                Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = findSheet(wb, mappingProperties.getCost().getSheetNames());
            if (sheet == null) {
                throw new IllegalArgumentException("未找到成本 Sheet，请使用: " + mappingProperties.getCost().getSheetNames());
            }
            int firstRow = sheet.getFirstRowNum();
            Row header0 = sheet.getRow(firstRow);
            Map<String, Integer> flatHeader = headerIndex(header0);
            boolean flatPsp = flatHeader.containsKey("PSP成本-1月");

            if (!flatPsp && isMergedTwoRowCostHeader(sheet, firstRow)) {
                MergedCostColumns merged = buildMergedCostColumns(sheet, firstRow);
                importMergedDataRows(sheet, merged, result);
            } else {
                importLegacyFlatHeader(sheet, firstRow, flatHeader, result);
            }
        }
        return result;
    }

    /** 第二行存在「N月」且首行不含扁平列「PSP成本-1月」时，按双行合并表头解析。 */
    private static boolean isMergedTwoRowCostHeader(Sheet sheet, int firstRow) {
        Row r1 = sheet.getRow(firstRow + 1);
        if (r1 == null) {
            return false;
        }
        int monthLabels = 0;
        int last = Math.max(lastCellIndex(sheet.getRow(firstRow)), lastCellIndex(r1));
        for (int c = 0; c <= last; c++) {
            String t1 = effectiveCellValue(sheet, firstRow + 1, c);
            if (t1 != null && MONTH_HEADER.matcher(normalizeHeaderText(t1)).matches()) {
                monthLabels++;
            }
        }
        return monthLabels >= 1;
    }

    private void importLegacyFlatHeader(Sheet sheet, int firstRow, Map<String, Integer> headerIdx, CostImportResult result) {
        for (String need : mappingProperties.getCost().getColumnMap().keySet()) {
            if (!headerIdx.containsKey(need)) {
                throw new IllegalArgumentException("成本表缺少列: " + need);
            }
        }
        for (int r = firstRow + 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null || rowIsEmpty(row)) {
                continue;
            }
            upsertOneLegacyRow(row, r + 1, headerIdx, result);
        }
    }

    private void upsertOneLegacyRow(Row row, int excelRow1Based, Map<String, Integer> headerIdx, CostImportResult result) {
        try {
            CostRow cost = readCostRowLegacy(row, headerIdx);
            if (cost.getDeviceId() == null || cost.getDeviceId().trim().isEmpty()) {
                result.getErrors().add("第" + excelRow1Based + "行: 编码为空");
                result.setFailCount(result.getFailCount() + 1);
                return;
            }
            cost.setDeviceId(cost.getDeviceId().trim());
            productCostDao.upsert(cost);
            result.setSuccessCount(result.getSuccessCount() + 1);
        } catch (Exception ex) {
            result.setFailCount(result.getFailCount() + 1);
            result.getErrors().add("第" + excelRow1Based + "行: " + ex.getMessage());
        }
    }

    private CostRow readCostRowLegacy(Row row, Map<String, Integer> headerIdx) {
        CostRow cost = new CostRow();
        cost.setDeviceId(text(row, headerIdx, "编码"));
        cost.setDescription(text(row, headerIdx, "编码描述"));
        cost.setDeviceType(text(row, headerIdx, "型号"));
        cost.setCurrency(text(row, headerIdx, "币种"));
        cost.setPspM01(decimalStrict(row, headerIdx, "PSP成本-1月"));
        cost.setPspM02(decimalStrict(row, headerIdx, "PSP成本-2月"));
        cost.setPspM03(decimalStrict(row, headerIdx, "PSP成本-3月"));
        cost.setPspM04(decimalStrict(row, headerIdx, "PSP成本-4月"));
        cost.setPspM05(decimalStrict(row, headerIdx, "PSP成本-5月"));
        cost.setPspM06(decimalStrict(row, headerIdx, "PSP成本-6月"));
        cost.setPspM07(decimalStrict(row, headerIdx, "PSP成本-7月"));
        cost.setPspM08(decimalStrict(row, headerIdx, "PSP成本-8月"));
        cost.setPspM09(decimalStrict(row, headerIdx, "PSP成本-9月"));
        cost.setPspM10(decimalStrict(row, headerIdx, "PSP成本-10月"));
        cost.setPspM11(decimalStrict(row, headerIdx, "PSP成本-11月"));
        cost.setPspM12(decimalStrict(row, headerIdx, "PSP成本-12月"));
        cost.setStdM01(decimalStrict(row, headerIdx, "标准成本-1月"));
        cost.setStdM02(decimalStrict(row, headerIdx, "标准成本-2月"));
        cost.setStdM03(decimalStrict(row, headerIdx, "标准成本-3月"));
        cost.setStdM04(decimalStrict(row, headerIdx, "标准成本-4月"));
        cost.setStdM05(decimalStrict(row, headerIdx, "标准成本-5月"));
        cost.setStdM06(decimalStrict(row, headerIdx, "标准成本-6月"));
        cost.setStdM07(decimalStrict(row, headerIdx, "标准成本-7月"));
        cost.setStdM08(decimalStrict(row, headerIdx, "标准成本-8月"));
        cost.setStdM09(decimalStrict(row, headerIdx, "标准成本-9月"));
        cost.setStdM10(decimalStrict(row, headerIdx, "标准成本-10月"));
        cost.setStdM11(decimalStrict(row, headerIdx, "标准成本-11月"));
        cost.setStdM12(decimalStrict(row, headerIdx, "标准成本-12月"));
        cost.setAdditionInfo(text(row, headerIdx, "备注"));
        return cost;
    }

    private void importMergedDataRows(Sheet sheet, MergedCostColumns m, CostImportResult result) {
        for (int r = m.dataStartRow; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null || rowIsEmpty(row)) {
                continue;
            }
            try {
                CostRow cost = readCostRowMerged(sheet, row, m);
                if (cost.getDeviceId() == null || cost.getDeviceId().trim().isEmpty()) {
                    result.getErrors().add("第" + (r + 1) + "行: 编码为空");
                    result.setFailCount(result.getFailCount() + 1);
                    continue;
                }
                cost.setDeviceId(cost.getDeviceId().trim());
                productCostDao.upsert(cost);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception ex) {
                result.setFailCount(result.getFailCount() + 1);
                result.getErrors().add("第" + (r + 1) + "行: " + ex.getMessage());
            }
        }
    }

    private static CostRow readCostRowMerged(Sheet sheet, Row row, MergedCostColumns m) {
        CostRow cost = new CostRow();
        cost.setDeviceId(textAt(row, m.deviceIdCol));
        cost.setDescription(textAt(row, m.descriptionCol));
        cost.setDeviceType(textAt(row, m.deviceTypeCol));
        cost.setCurrency(textAt(row, m.currencyCol));
        cost.setAdditionInfo(textAt(row, m.remarkCol));
        for (int month = 1; month <= 12; month++) {
            BigDecimal psp = decimalAtNullable(row, m.pspCol[month]);
            BigDecimal std = decimalAtNullable(row, m.stdCol[month]);
            setPspMonth(cost, month, psp);
            setStdMonth(cost, month, std);
        }
        return cost;
    }

    private static void setPspMonth(CostRow cost, int month, BigDecimal v) {
        switch (month) {
            case 1:
                cost.setPspM01(v);
                break;
            case 2:
                cost.setPspM02(v);
                break;
            case 3:
                cost.setPspM03(v);
                break;
            case 4:
                cost.setPspM04(v);
                break;
            case 5:
                cost.setPspM05(v);
                break;
            case 6:
                cost.setPspM06(v);
                break;
            case 7:
                cost.setPspM07(v);
                break;
            case 8:
                cost.setPspM08(v);
                break;
            case 9:
                cost.setPspM09(v);
                break;
            case 10:
                cost.setPspM10(v);
                break;
            case 11:
                cost.setPspM11(v);
                break;
            case 12:
                cost.setPspM12(v);
                break;
            default:
                break;
        }
    }

    private static void setStdMonth(CostRow cost, int month, BigDecimal v) {
        switch (month) {
            case 1:
                cost.setStdM01(v);
                break;
            case 2:
                cost.setStdM02(v);
                break;
            case 3:
                cost.setStdM03(v);
                break;
            case 4:
                cost.setStdM04(v);
                break;
            case 5:
                cost.setStdM05(v);
                break;
            case 6:
                cost.setStdM06(v);
                break;
            case 7:
                cost.setStdM07(v);
                break;
            case 8:
                cost.setStdM08(v);
                break;
            case 9:
                cost.setStdM09(v);
                break;
            case 10:
                cost.setStdM10(v);
                break;
            case 11:
                cost.setStdM11(v);
                break;
            case 12:
                cost.setStdM12(v);
                break;
            default:
                break;
        }
    }

    private static MergedCostColumns buildMergedCostColumns(Sheet sheet, int firstRow) {
        int lastCol = 0;
        Row r0 = sheet.getRow(firstRow);
        Row r1 = sheet.getRow(firstRow + 1);
        if (r0 != null) {
            lastCol = Math.max(lastCol, lastCellIndex(r0));
        }
        if (r1 != null) {
            lastCol = Math.max(lastCol, lastCellIndex(r1));
        }
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress reg = sheet.getMergedRegion(i);
            if (reg.getFirstRow() <= firstRow + 1 && reg.getLastRow() >= firstRow) {
                lastCol = Math.max(lastCol, reg.getLastColumn());
            }
        }

        MergedCostColumns m = new MergedCostColumns();
        m.dataStartRow = firstRow + 2;
        String activeMonthSection = null;

        for (int c = 0; c <= lastCol; c++) {
            String t0 = effectiveCellValue(sheet, firstRow, c);
            String t1 = effectiveCellValue(sheet, firstRow + 1, c);
            if (t0 != null && !t0.isEmpty()) {
                String h0 = normalizeHeaderText(t0);
                if (h0.contains("PSP") && h0.contains("成本")) {
                    activeMonthSection = "PSP";
                } else if (h0.contains("标准") && h0.contains("成本")) {
                    activeMonthSection = "STD";
                } else if ("编码".equals(h0)) {
                    m.deviceIdCol = c;
                } else if ("编码描述".equals(h0)) {
                    m.descriptionCol = c;
                } else if ("型号".equals(h0)) {
                    m.deviceTypeCol = c;
                } else if ("币种".equals(h0)) {
                    m.currencyCol = c;
                } else if ("备注".equals(h0)) {
                    m.remarkCol = c;
                    activeMonthSection = null;
                }
            }
            if (t1 != null && !t1.isEmpty()) {
                String h1 = normalizeHeaderText(t1);
                Matcher mm = MONTH_HEADER.matcher(h1);
                if (mm.matches()) {
                    int month = Integer.parseInt(mm.group(1));
                    if (month < 1 || month > 12) {
                        throw new IllegalArgumentException("表头第" + (firstRow + 2) + "行第" + (c + 1) + "列: 非法月份「" + t1 + "」");
                    }
                    if ("PSP".equals(activeMonthSection)) {
                        if (m.pspCol[month] >= 0) {
                            throw new IllegalArgumentException("PSP 成本列「" + month + "月」重复映射");
                        }
                        m.pspCol[month] = c;
                    } else if ("STD".equals(activeMonthSection)) {
                        if (m.stdCol[month] >= 0) {
                            throw new IllegalArgumentException("标准成本列「" + month + "月」重复映射");
                        }
                        m.stdCol[month] = c;
                    }
                }
            }
        }

        if (m.deviceIdCol < 0) {
            throw new IllegalArgumentException("合并表头中未找到「编码」列（请检查纵向合并后左上角单元格是否为「编码」）");
        }
        boolean anyPsp = false;
        boolean anyStd = false;
        for (int i = 1; i <= 12; i++) {
            if (m.pspCol[i] >= 0) {
                anyPsp = true;
            }
            if (m.stdCol[i] >= 0) {
                anyStd = true;
            }
        }
        if (!anyPsp && !anyStd) {
            throw new IllegalArgumentException("合并表头中未解析到 PSP 或标准成本的月份列（需在「PSP成本」「标准成本」分组下第二行使用「1月」…「12月」样式）");
        }
        return m;
    }

    private static String normalizeHeaderText(String s) {
        if (s == null) {
            return "";
        }
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFKC);
    }

    /**
     * 读取单元格显示值；若为空且在合并区域内，则取合并区域左上角单元格的值（处理行列合并）。
     */
    private static String effectiveCellValue(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        Cell cell = row != null ? row.getCell(colIdx) : null;
        String direct = cellString(cell);
        if (direct != null && !direct.isEmpty()) {
            return direct.trim();
        }
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(rowIdx, colIdx)) {
                Row top = sheet.getRow(region.getFirstRow());
                if (top == null) {
                    continue;
                }
                Cell origin = top.getCell(region.getFirstColumn());
                String v = cellString(origin);
                if (v != null && !v.isEmpty()) {
                    return v.trim();
                }
            }
        }
        return null;
    }

    private static int lastCellIndex(Row row) {
        if (row == null) {
            return 0;
        }
        short n = row.getLastCellNum();
        return n > 0 ? n - 1 : 0;
    }

    private static String textAt(Row row, int col) {
        if (row == null || col < 0) {
            return null;
        }
        return cellString(row.getCell(col));
    }

    private static BigDecimal decimalAtNullable(Row row, int col) {
        if (row == null || col < 0) {
            return null;
        }
        String t = cellString(row.getCell(col));
        if (t == null || t.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(t.replace(",", "").replace("%", "").trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("第" + (col + 1) + "列非数字: " + t);
        }
    }

    static final class MergedCostColumns {
        int dataStartRow;
        int deviceIdCol = -1;
        int descriptionCol = -1;
        int deviceTypeCol = -1;
        int currencyCol = -1;
        int remarkCol = -1;
        final int[] pspCol = new int[13];
        final int[] stdCol = new int[13];

        MergedCostColumns() {
            for (int i = 1; i <= 12; i++) {
                pspCol[i] = -1;
                stdCol[i] = -1;
            }
        }
    }

    private static BigDecimal decimalStrict(Row row, Map<String, Integer> header, String name) {
        String t = text(row, header, name);
        if (t == null) {
            return null;
        }
        try {
            return new BigDecimal(t.replace(",", "").trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("列[" + name + "]非数字: " + t);
        }
    }

    public static class CostImportResult {
        private int successCount;
        private int failCount;
        private final List<String> errors = new ArrayList<>();

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public void setFailCount(int failCount) {
            this.failCount = failCount;
        }

        public List<String> getErrors() {
            return errors;
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

    private static boolean rowIsEmpty(Row row) {
        for (Cell c : row) {
            String t = cellString(c);
            if (t != null && !t.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String cellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        String t = FORMATTER.formatCellValue(cell).trim();
        return t.isEmpty() ? null : t;
    }

    private static String text(Row row, Map<String, Integer> header, String name) {
        Integer col = header.get(name);
        if (col == null || row == null) {
            return null;
        }
        return cellString(row.getCell(col));
    }
}
