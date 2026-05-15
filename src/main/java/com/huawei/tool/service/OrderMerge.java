package com.huawei.tool.service;

import com.huawei.tool.dao.model.OrderRow;

public final class OrderMerge {

    private OrderMerge() {
    }

    public static OrderRow merge(OrderRow existing, OrderRow patch) {
        if (patch.getArea() != null) {
            existing.setArea(patch.getArea());
        }
        if (patch.getRepresentativeOffice() != null) {
            existing.setRepresentativeOffice(patch.getRepresentativeOffice());
        }
        if (patch.getCountry() != null) {
            existing.setCountry(patch.getCountry());
        }
        if (patch.getAccounts() != null) {
            existing.setAccounts(patch.getAccounts());
        }
        if (patch.getProject() != null) {
            existing.setProject(patch.getProject());
        }
        if (patch.getPoId() != null) {
            existing.setPoId(patch.getPoId());
        }
        return existing;
    }
}
