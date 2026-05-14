package com.huawei.tool.dao.model;

import java.math.BigDecimal;

public class DetailRow {
    private String id;
    private String taskId;
    private String orderId;
    private String deviceType;
    private String deviceId;
    private String description;
    private String categoryType;
    private String hardwareType;
    private String scene;
    private Integer deviceCount;
    private String incomeMonth;
    private String currency;
    private BigDecimal listPrice;
    private BigDecimal beforeTier;
    private BigDecimal beforePrice;
    private String discountDescBefore;
    private BigDecimal afterTier;
    private BigDecimal afterPrice;
    private String discountDescAfter;
    private BigDecimal tierIncrease;
    private BigDecimal priceIncrease;
    private BigDecimal afterTierDiscountRate;
    private BigDecimal afterPriceDiscountRate;
    private BigDecimal beforeTotalPrice;
    private BigDecimal afterTotalPrice;
    private BigDecimal totalPriceIncrease;
    private BigDecimal totalIncreaseRate;
    private String additionInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getHardwareType() {
        return hardwareType;
    }

    public void setHardwareType(String hardwareType) {
        this.hardwareType = hardwareType;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public Integer getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(Integer deviceCount) {
        this.deviceCount = deviceCount;
    }

    public String getIncomeMonth() {
        return incomeMonth;
    }

    public void setIncomeMonth(String incomeMonth) {
        this.incomeMonth = incomeMonth;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getListPrice() {
        return listPrice;
    }

    public void setListPrice(BigDecimal listPrice) {
        this.listPrice = listPrice;
    }

    public BigDecimal getBeforeTier() {
        return beforeTier;
    }

    public void setBeforeTier(BigDecimal beforeTier) {
        this.beforeTier = beforeTier;
    }

    public BigDecimal getBeforePrice() {
        return beforePrice;
    }

    public void setBeforePrice(BigDecimal beforePrice) {
        this.beforePrice = beforePrice;
    }

    public String getDiscountDescBefore() {
        return discountDescBefore;
    }

    public void setDiscountDescBefore(String discountDescBefore) {
        this.discountDescBefore = discountDescBefore;
    }

    public BigDecimal getAfterTier() {
        return afterTier;
    }

    public void setAfterTier(BigDecimal afterTier) {
        this.afterTier = afterTier;
    }

    public BigDecimal getAfterPrice() {
        return afterPrice;
    }

    public void setAfterPrice(BigDecimal afterPrice) {
        this.afterPrice = afterPrice;
    }

    public String getDiscountDescAfter() {
        return discountDescAfter;
    }

    public void setDiscountDescAfter(String discountDescAfter) {
        this.discountDescAfter = discountDescAfter;
    }

    public BigDecimal getTierIncrease() {
        return tierIncrease;
    }

    public void setTierIncrease(BigDecimal tierIncrease) {
        this.tierIncrease = tierIncrease;
    }

    public BigDecimal getPriceIncrease() {
        return priceIncrease;
    }

    public void setPriceIncrease(BigDecimal priceIncrease) {
        this.priceIncrease = priceIncrease;
    }

    public BigDecimal getAfterTierDiscountRate() {
        return afterTierDiscountRate;
    }

    public void setAfterTierDiscountRate(BigDecimal afterTierDiscountRate) {
        this.afterTierDiscountRate = afterTierDiscountRate;
    }

    public BigDecimal getAfterPriceDiscountRate() {
        return afterPriceDiscountRate;
    }

    public void setAfterPriceDiscountRate(BigDecimal afterPriceDiscountRate) {
        this.afterPriceDiscountRate = afterPriceDiscountRate;
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

    public BigDecimal getTotalIncreaseRate() {
        return totalIncreaseRate;
    }

    public void setTotalIncreaseRate(BigDecimal totalIncreaseRate) {
        this.totalIncreaseRate = totalIncreaseRate;
    }

    public String getAdditionInfo() {
        return additionInfo;
    }

    public void setAdditionInfo(String additionInfo) {
        this.additionInfo = additionInfo;
    }
}
