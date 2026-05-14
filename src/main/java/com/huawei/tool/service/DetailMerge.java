package com.huawei.tool.service;

import com.huawei.tool.dao.model.DetailRow;

import java.math.BigDecimal;
import java.util.function.Consumer;

public final class DetailMerge {

    private DetailMerge() {
    }

    public static DetailRow merge(DetailRow existing, DetailRow patch) {
        if (patch.getDeviceType() != null) {
            existing.setDeviceType(patch.getDeviceType());
        }
        if (patch.getDeviceId() != null) {
            existing.setDeviceId(patch.getDeviceId());
        }
        if (patch.getDescription() != null) {
            existing.setDescription(patch.getDescription());
        }
        if (patch.getCategoryType() != null) {
            existing.setCategoryType(patch.getCategoryType());
        }
        if (patch.getHardwareType() != null) {
            existing.setHardwareType(patch.getHardwareType());
        }
        if (patch.getScene() != null) {
            existing.setScene(patch.getScene());
        }
        if (patch.getDeviceCount() != null) {
            existing.setDeviceCount(patch.getDeviceCount());
        }
        if (patch.getIncomeMonth() != null) {
            existing.setIncomeMonth(patch.getIncomeMonth());
        }
        if (patch.getCurrency() != null) {
            existing.setCurrency(patch.getCurrency());
        }
        copyDecimal(patch.getListPrice(), existing::setListPrice);
        copyDecimal(patch.getBeforeTier(), existing::setBeforeTier);
        copyDecimal(patch.getBeforePrice(), existing::setBeforePrice);
        if (patch.getDiscountDescBefore() != null) {
            existing.setDiscountDescBefore(patch.getDiscountDescBefore());
        }
        copyDecimal(patch.getAfterTier(), existing::setAfterTier);
        copyDecimal(patch.getAfterPrice(), existing::setAfterPrice);
        if (patch.getDiscountDescAfter() != null) {
            existing.setDiscountDescAfter(patch.getDiscountDescAfter());
        }
        copyDecimal(patch.getTierIncrease(), existing::setTierIncrease);
        copyDecimal(patch.getPriceIncrease(), existing::setPriceIncrease);
        copyDecimal(patch.getAfterTierDiscountRate(), existing::setAfterTierDiscountRate);
        copyDecimal(patch.getAfterPriceDiscountRate(), existing::setAfterPriceDiscountRate);
        copyDecimal(patch.getBeforeTotalPrice(), existing::setBeforeTotalPrice);
        copyDecimal(patch.getAfterTotalPrice(), existing::setAfterTotalPrice);
        copyDecimal(patch.getTotalPriceIncrease(), existing::setTotalPriceIncrease);
        copyDecimal(patch.getTotalIncreaseRate(), existing::setTotalIncreaseRate);
        if (patch.getAdditionInfo() != null) {
            existing.setAdditionInfo(patch.getAdditionInfo());
        }
        return existing;
    }

    private static void copyDecimal(BigDecimal v, Consumer<BigDecimal> setter) {
        if (v != null) {
            setter.accept(v);
        }
    }
}
