package com.huawei.tool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "excel")
public class ExcelMappingProperties {

    private Order order = new Order();
    private Cost cost = new Cost();

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Cost getCost() {
        return cost;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
    }

    public static class Order {
        /**
         * 单 Sheet：一行同时包含订单头与明细列，解析后分别写入订单表与明细表。
         * 须非空，并在列表中配置工作簿里实际 Sheet 名（按顺序命中第一个）。
         */
        private List<String> orderDataSheetNames = new ArrayList<>();
        private Map<String, String> orderColumnMap = new LinkedHashMap<>();
        private Map<String, String> detailColumnMap = new LinkedHashMap<>();

        public List<String> getOrderDataSheetNames() {
            return orderDataSheetNames;
        }

        public void setOrderDataSheetNames(List<String> orderDataSheetNames) {
            this.orderDataSheetNames = orderDataSheetNames;
        }

        public Map<String, String> getOrderColumnMap() {
            return orderColumnMap;
        }

        public void setOrderColumnMap(Map<String, String> orderColumnMap) {
            this.orderColumnMap = orderColumnMap;
        }

        public Map<String, String> getDetailColumnMap() {
            return detailColumnMap;
        }

        public void setDetailColumnMap(Map<String, String> detailColumnMap) {
            this.detailColumnMap = detailColumnMap;
        }
    }

    public static class Cost {
        private List<String> sheetNames = new ArrayList<>();
        private Map<String, String> columnMap = new LinkedHashMap<>();

        public List<String> getSheetNames() {
            return sheetNames;
        }

        public void setSheetNames(List<String> sheetNames) {
            this.sheetNames = sheetNames;
        }

        public Map<String, String> getColumnMap() {
            return columnMap;
        }

        public void setColumnMap(Map<String, String> columnMap) {
            this.columnMap = columnMap;
        }
    }
}
