-- 一次性迁移：将「销毛」从 t_order_info 迁至 t_order_product_detail_data（行级）。
-- 执行前请备份。若 t_order_product_detail_data 已含 gross_profit 列，请勿重复执行 ADD。

ALTER TABLE t_order_product_detail_data
    ADD COLUMN gross_profit DECIMAL(12,2) NULL AFTER total_increase_rate;

UPDATE t_order_product_detail_data d
INNER JOIN t_order_info o ON d.task_id = o.task_id AND d.order_id = o.order_id
SET d.gross_profit = o.gross_profit
WHERE o.gross_profit IS NOT NULL;

ALTER TABLE t_order_info DROP COLUMN gross_profit;
