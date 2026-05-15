-- 订单头增加「产品领域」；汇总表增加「产品领域」与「价格涨幅」price_increase_rate
ALTER TABLE t_order_info ADD COLUMN product_domain VARCHAR(64) NULL AFTER po_id;
ALTER TABLE t_summary_report ADD COLUMN product_domain VARCHAR(64) NULL AFTER po_id;
ALTER TABLE t_summary_report ADD COLUMN price_increase_rate DECIMAL(7,4) NULL AFTER total_price_increase;
