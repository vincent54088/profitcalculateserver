package com.huawei.tool.service;

import com.huawei.tool.dao.OrderDetailDao;
import com.huawei.tool.dao.OrderInfoDao;
import com.huawei.tool.dao.ProductCostDao;
import com.huawei.tool.dao.SummaryReportDao;
import com.huawei.tool.dao.model.CostRow;
import com.huawei.tool.dao.model.DetailRow;
import com.huawei.tool.dao.model.OrderRow;
import com.huawei.tool.dao.model.SummaryRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfitCalculationService {

    private static final Logger log = LoggerFactory.getLogger(ProfitCalculationService.class);

    private final OrderDetailDao orderDetailDao;
    private final OrderInfoDao orderInfoDao;
    private final ProductCostDao productCostDao;
    private final SummaryReportDao summaryReportDao;

    public ProfitCalculationService(
            OrderDetailDao orderDetailDao,
            OrderInfoDao orderInfoDao,
            ProductCostDao productCostDao,
            SummaryReportDao summaryReportDao) {
        this.orderDetailDao = orderDetailDao;
        this.orderInfoDao = orderInfoDao;
        this.productCostDao = productCostDao;
        this.summaryReportDao = summaryReportDao;
    }

    @Transactional
    public void recalculate(String taskId) {
        summaryReportDao.deleteByTask(taskId);
        List<DetailRow> details = orderDetailDao.findAllByTask(taskId);
        Map<String, OrderRow> orders = orderInfoDao.mapByOrderId(taskId);
        Map<String, CostRow> costs = productCostDao.findAllAsMap();

        Map<String, List<DetailRow>> groups = new HashMap<>();
        for (DetailRow d : details) {
            String monthRaw = d.getIncomeMonth() == null ? "" : d.getIncomeMonth().trim();
            String key = d.getOrderId() + "\u0001" + monthRaw;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(d);
        }

        List<SummaryRow> out = new ArrayList<>();
        for (Map.Entry<String, List<DetailRow>> e : groups.entrySet()) {
            String[] parts = e.getKey().split("\u0001", 2);
            String orderId = parts[0];
            String monthRaw = parts.length > 1 ? parts[1] : "";
            List<DetailRow> lines = e.getValue();

            OrderRow order = orders.get(orderId);
            if (order == null) {
                log.warn("明细存在但缺少订单头: taskId={} orderId={}", taskId, orderId);
                continue;
            }

            String monthKey = MonthColumnResolver.normalizeMonthKey(monthRaw);

            BigDecimal pspTotal = BigDecimal.ZERO;
            BigDecimal stdTotal = BigDecimal.ZERO;
            BigDecimal hwRevenue = BigDecimal.ZERO;
            BigDecimal hwBeforeSum = BigDecimal.ZERO;
            BigDecimal hwAfterSum = BigDecimal.ZERO;
            BigDecimal swBeforeSum = BigDecimal.ZERO;
            BigDecimal swAfterSum = BigDecimal.ZERO;

            for (DetailRow d : lines) {
                String cat = d.getCategoryType();
                int qty = d.getDeviceCount() == null ? 0 : d.getDeviceCount();
                BigDecimal q = BigDecimal.valueOf(qty);
                if (isSoftware(cat)) {
                    swBeforeSum = swBeforeSum.add(nz(d.getBeforeTotalPrice()));
                    swAfterSum = swAfterSum.add(nz(d.getAfterTotalPrice()));
                } else if (isHardware(cat)) {
                    BigDecimal unitPsp = BigDecimal.ZERO;
                    BigDecimal unitStd = BigDecimal.ZERO;
                    if (monthKey != null && d.getDeviceId() != null) {
                        CostRow cr = costs.get(d.getDeviceId());
                        unitPsp = productCostDao.getPspMonth(cr, monthKey);
                        unitStd = productCostDao.getStdMonth(cr, monthKey);
                        if (cr == null) {
                            log.debug("缺少成本 device_id={} taskId={}", d.getDeviceId(), taskId);
                        }
                    }
                    pspTotal = pspTotal.add(unitPsp.multiply(q));
                    stdTotal = stdTotal.add(unitStd.multiply(q));
                    BigDecimal ap = nz(d.getAfterPrice());
                    hwRevenue = hwRevenue.add(ap.multiply(q));
                    hwBeforeSum = hwBeforeSum.add(nz(d.getBeforeTotalPrice()));
                    hwAfterSum = hwAfterSum.add(nz(d.getAfterTotalPrice()));
                } else if (cat != null && !cat.trim().isEmpty()) {
                    log.debug("未识别软硬件分类: {} orderId={} lineId={}", cat, orderId, d.getId());
                }
            }

            BigDecimal hwPspGp = null;
            BigDecimal hwStdGp = null;
            if (hwRevenue.compareTo(BigDecimal.ZERO) > 0) {
                hwPspGp = BigDecimal.ONE.subtract(pspTotal.divide(hwRevenue, 8, RoundingMode.HALF_UP))
                        .setScale(4, RoundingMode.HALF_UP);
                hwStdGp = BigDecimal.ONE.subtract(stdTotal.divide(hwRevenue, 8, RoundingMode.HALF_UP))
                        .setScale(4, RoundingMode.HALF_UP);
            }

            BigDecimal totalPriceIncrease = hwAfterSum.subtract(hwBeforeSum);
            BigDecimal swRate = null;
            if (swBeforeSum.compareTo(BigDecimal.ZERO) > 0) {
                swRate = BigDecimal.ONE.subtract(swAfterSum.divide(swBeforeSum, 8, RoundingMode.HALF_UP))
                        .setScale(4, RoundingMode.HALF_UP);
            }

            SummaryRow s = new SummaryRow();
            s.setTaskId(taskId);
            s.setOrderId(orderId);
            s.setIncomeMonth(monthRaw.isEmpty() ? "-" : monthRaw);
            s.setArea(order.getArea());
            s.setRepresentativeOffice(order.getRepresentativeOffice());
            s.setCountry(order.getCountry());
            s.setAccounts(order.getAccounts());
            s.setProject(order.getProject());
            s.setPoId(order.getPoId());
            s.setGrossProfit(order.getGrossProfit());
            s.setHwPspGrossProfit(hwPspGp);
            s.setHwStandardGrossProfit(hwStdGp);
            s.setBeforeTotalPrice(hwBeforeSum);
            s.setAfterTotalPrice(hwAfterSum);
            s.setTotalPriceIncrease(totalPriceIncrease);
            s.setSoftwareHistoryPrice(swBeforeSum);
            s.setSoftwarePrice(swAfterSum);
            s.setSoftwarePriceIncreaseRate(swRate);
            out.add(s);
        }

        if (!out.isEmpty()) {
            summaryReportDao.batchInsert(out);
        }
    }

    private static boolean isHardware(String categoryType) {
        if (categoryType == null) {
            return false;
        }
        String t = categoryType.trim();
        return t.contains("硬件");
    }

    private static boolean isSoftware(String categoryType) {
        if (categoryType == null) {
            return false;
        }
        String t = categoryType.trim();
        return t.contains("软件") && !t.contains("硬件");
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
