-- 汇算结果改为按任务+合同唯一一行（不按月份拆分主键）。执行前请备份。
-- 会清空评测汇总表；部署新代码后请对任务重新触发汇算以回填。

DELETE FROM t_summary_report;

ALTER TABLE t_summary_report DROP PRIMARY KEY,
    ADD PRIMARY KEY (task_id, order_id);
