package com.huawei.tool.dao;

import com.huawei.tool.dao.model.CostRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface ProductCostDao {

    List<CostRow> selectAll();

    long countAll();

    List<CostRow> selectPage(@Param("offset") int offset, @Param("limit") int limit);

    CostRow selectByDeviceId(@Param("deviceId") String deviceId);

    int upsert(CostRow r);

    default Map<String, CostRow> findAllAsMap() {
        Map<String, CostRow> m = new HashMap<>();
        for (CostRow row : selectAll()) {
            m.put(row.getDeviceId(), row);
        }
        return m;
    }

    default Optional<CostRow> findByDeviceId(String deviceId) {
        CostRow r = selectByDeviceId(deviceId);
        return r == null ? Optional.empty() : Optional.of(r);
    }

    default BigDecimal getPspMonth(CostRow row, String monthKey) {
        if (row == null || monthKey == null) {
            return BigDecimal.ZERO;
        }
        switch (monthKey) {
            case "01":
                return ProductCostDao.nz(row.getPspM01());
            case "02":
                return ProductCostDao.nz(row.getPspM02());
            case "03":
                return ProductCostDao.nz(row.getPspM03());
            case "04":
                return ProductCostDao.nz(row.getPspM04());
            case "05":
                return ProductCostDao.nz(row.getPspM05());
            case "06":
                return ProductCostDao.nz(row.getPspM06());
            case "07":
                return ProductCostDao.nz(row.getPspM07());
            case "08":
                return ProductCostDao.nz(row.getPspM08());
            case "09":
                return ProductCostDao.nz(row.getPspM09());
            case "10":
                return ProductCostDao.nz(row.getPspM10());
            case "11":
                return ProductCostDao.nz(row.getPspM11());
            case "12":
                return ProductCostDao.nz(row.getPspM12());
            default:
                return BigDecimal.ZERO;
        }
    }

    default BigDecimal getStdMonth(CostRow row, String monthKey) {
        if (row == null || monthKey == null) {
            return BigDecimal.ZERO;
        }
        switch (monthKey) {
            case "01":
                return ProductCostDao.nz(row.getStdM01());
            case "02":
                return ProductCostDao.nz(row.getStdM02());
            case "03":
                return ProductCostDao.nz(row.getStdM03());
            case "04":
                return ProductCostDao.nz(row.getStdM04());
            case "05":
                return ProductCostDao.nz(row.getStdM05());
            case "06":
                return ProductCostDao.nz(row.getStdM06());
            case "07":
                return ProductCostDao.nz(row.getStdM07());
            case "08":
                return ProductCostDao.nz(row.getStdM08());
            case "09":
                return ProductCostDao.nz(row.getStdM09());
            case "10":
                return ProductCostDao.nz(row.getStdM10());
            case "11":
                return ProductCostDao.nz(row.getStdM11());
            case "12":
                return ProductCostDao.nz(row.getStdM12());
            default:
                return BigDecimal.ZERO;
        }
    }

    static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
