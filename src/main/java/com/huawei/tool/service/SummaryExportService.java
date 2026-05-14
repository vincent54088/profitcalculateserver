package com.huawei.tool.service;

import com.huawei.tool.dao.SummaryReportDao;
import com.huawei.tool.dao.model.SummaryRow;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Service
public class SummaryExportService {

    private static final List<String> EXPORT_HEADERS = Arrays.asList(
            "合同号", "区域", "代表处", "国家", "客户群", "项目", "PO号", "销毛",
            "收入确定时间", "硬件PSP制毛", "硬件标准口径制毛", "提价前总价格", "提价后总价格", "价格上涨",
            "软件历史价格", "本次软件价格", "软件价格变化");

    private final SummaryReportDao summaryReportDao;

    public SummaryExportService(SummaryReportDao summaryReportDao) {
        this.summaryReportDao = summaryReportDao;
    }

    public Path exportTaskSummary(String taskId) throws Exception {
        List<SummaryRow> rows = summaryReportDao.findAllByTask(taskId);
        Path out = Files.createTempFile("summary-export-" + taskId + "-", ".xlsx");
        SXSSFWorkbook wb = new SXSSFWorkbook(100);
        try {
            Sheet sh = wb.createSheet("评测结果");
            Row h = sh.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.size(); i++) {
                h.createCell(i).setCellValue(EXPORT_HEADERS.get(i));
            }
            int r = 1;
            for (SummaryRow row : rows) {
                Row xr = sh.createRow(r++);
                int c = 0;
                xr.createCell(c++).setCellValue(nz(row.getOrderId()));
                xr.createCell(c++).setCellValue(nz(row.getArea()));
                xr.createCell(c++).setCellValue(nz(row.getRepresentativeOffice()));
                xr.createCell(c++).setCellValue(nz(row.getCountry()));
                xr.createCell(c++).setCellValue(nz(row.getAccounts()));
                xr.createCell(c++).setCellValue(nz(row.getProject()));
                xr.createCell(c++).setCellValue(nz(row.getPoId()));
                setNum(xr.createCell(c++), row.getGrossProfit());
                xr.createCell(c++).setCellValue(nz(row.getIncomeMonth()));
                setNum(xr.createCell(c++), row.getHwPspGrossProfit());
                setNum(xr.createCell(c++), row.getHwStandardGrossProfit());
                setNum(xr.createCell(c++), row.getBeforeTotalPrice());
                setNum(xr.createCell(c++), row.getAfterTotalPrice());
                setNum(xr.createCell(c++), row.getTotalPriceIncrease());
                setNum(xr.createCell(c++), row.getSoftwareHistoryPrice());
                setNum(xr.createCell(c++), row.getSoftwarePrice());
                setNum(xr.createCell(c++), row.getSoftwarePriceIncreaseRate());
            }
            try (OutputStream os = Files.newOutputStream(out)) {
                wb.write(os);
            }
        } finally {
            wb.dispose();
            wb.close();
        }
        return out;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static void setNum(org.apache.poi.ss.usermodel.Cell cell, BigDecimal v) {
        if (v == null) {
            cell.setBlank();
        } else {
            cell.setCellValue(v.doubleValue());
        }
    }
}
