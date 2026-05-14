package com.huawei.tool.dao.model;

import java.math.BigDecimal;

/**
 * 分页预览用：一行明细 + 其所属订单头字段（与导入 Excel 列一致）。
 */
public class OrderDetailPageItem extends DetailRow {

    private String area;
    private String representativeOffice;
    private String country;
    private String accounts;
    private String project;
    private String poId;
    private BigDecimal grossProfit;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getRepresentativeOffice() {
        return representativeOffice;
    }

    public void setRepresentativeOffice(String representativeOffice) {
        this.representativeOffice = representativeOffice;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAccounts() {
        return accounts;
    }

    public void setAccounts(String accounts) {
        this.accounts = accounts;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getPoId() {
        return poId;
    }

    public void setPoId(String poId) {
        this.poId = poId;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }
}
