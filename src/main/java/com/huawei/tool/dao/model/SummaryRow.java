package com.huawei.tool.dao.model;

import java.math.BigDecimal;

public class SummaryRow {
    private String taskId;
    /** 跨任务列表查询时由 JOIN 填充，单任务分页可为空 */
    private String taskName;
    private String orderId;
    private String incomeMonth;
    private String area;
    private String representativeOffice;
    private String country;
    private String accounts;
    private String project;
    private String poId;
    private String productDomain;
    private BigDecimal grossProfit;
    private BigDecimal hwPspGrossProfit;
    private BigDecimal hwStandardGrossProfit;
    private BigDecimal beforeTotalPrice;
    private BigDecimal afterTotalPrice;
    private BigDecimal totalPriceIncrease;
    private BigDecimal priceIncreaseRate;
    private BigDecimal softwareHistoryPrice;
    private BigDecimal softwarePrice;
    private BigDecimal softwarePriceIncreaseRate;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getIncomeMonth() {
        return incomeMonth;
    }

    public void setIncomeMonth(String incomeMonth) {
        this.incomeMonth = incomeMonth;
    }

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

    public String getProductDomain() {
        return productDomain;
    }

    public void setProductDomain(String productDomain) {
        this.productDomain = productDomain;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }

    public BigDecimal getHwPspGrossProfit() {
        return hwPspGrossProfit;
    }

    public void setHwPspGrossProfit(BigDecimal hwPspGrossProfit) {
        this.hwPspGrossProfit = hwPspGrossProfit;
    }

    public BigDecimal getHwStandardGrossProfit() {
        return hwStandardGrossProfit;
    }

    public void setHwStandardGrossProfit(BigDecimal hwStandardGrossProfit) {
        this.hwStandardGrossProfit = hwStandardGrossProfit;
    }

    public BigDecimal getBeforeTotalPrice() {
        return beforeTotalPrice;
    }

    public void setBeforeTotalPrice(BigDecimal beforeTotalPrice) {
        this.beforeTotalPrice = beforeTotalPrice;
    }

    public BigDecimal getAfterTotalPrice() {
        return afterTotalPrice;
    }

    public void setAfterTotalPrice(BigDecimal afterTotalPrice) {
        this.afterTotalPrice = afterTotalPrice;
    }

    public BigDecimal getTotalPriceIncrease() {
        return totalPriceIncrease;
    }

    public void setTotalPriceIncrease(BigDecimal totalPriceIncrease) {
        this.totalPriceIncrease = totalPriceIncrease;
    }

    public BigDecimal getPriceIncreaseRate() {
        return priceIncreaseRate;
    }

    public void setPriceIncreaseRate(BigDecimal priceIncreaseRate) {
        this.priceIncreaseRate = priceIncreaseRate;
    }

    public BigDecimal getSoftwareHistoryPrice() {
        return softwareHistoryPrice;
    }

    public void setSoftwareHistoryPrice(BigDecimal softwareHistoryPrice) {
        this.softwareHistoryPrice = softwareHistoryPrice;
    }

    public BigDecimal getSoftwarePrice() {
        return softwarePrice;
    }

    public void setSoftwarePrice(BigDecimal softwarePrice) {
        this.softwarePrice = softwarePrice;
    }

    public BigDecimal getSoftwarePriceIncreaseRate() {
        return softwarePriceIncreaseRate;
    }

    public void setSoftwarePriceIncreaseRate(BigDecimal softwarePriceIncreaseRate) {
        this.softwarePriceIncreaseRate = softwarePriceIncreaseRate;
    }
}
