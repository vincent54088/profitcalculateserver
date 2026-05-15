package com.huawei.tool.dao.model;

public class OrderRow {
    private String orderId;
    private String area;
    private String representativeOffice;
    private String country;
    private String accounts;
    private String project;
    private String poId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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
}
