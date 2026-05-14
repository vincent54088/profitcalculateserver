package com.huawei.tool.service;

import com.huawei.tool.dao.ProductCostDao;
import com.huawei.tool.dao.model.CostRow;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductCostManageService {

    private final ProductCostDao productCostDao;

    public ProductCostManageService(ProductCostDao productCostDao) {
        this.productCostDao = productCostDao;
    }

    public Map<String, Object> pageProductCosts(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 500);
        long total = productCostDao.countAll();
        int offset = Math.max(page, 0) * safeSize;
        List<CostRow> items = productCostDao.selectPage(offset, safeSize);
        Map<String, Object> m = new HashMap<>();
        m.put("total", total);
        m.put("items", items);
        return m;
    }

    /**
     * 在线编辑保存：按 device_id upsert，与 Excel 导入写入规则一致。
     */
    public void saveCost(CostRow row) {
        if (row.getDeviceId() == null || row.getDeviceId().trim().isEmpty()) {
            throw new IllegalArgumentException("编码不能为空");
        }
        row.setDeviceId(row.getDeviceId().trim());
        row.setDescription(trimToNull(row.getDescription()));
        row.setDeviceType(trimToNull(row.getDeviceType()));
        row.setCurrency(trimToNull(row.getCurrency()));
        row.setAdditionInfo(trimToNull(row.getAdditionInfo()));
        productCostDao.upsert(row);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
