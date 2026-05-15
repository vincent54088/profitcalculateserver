CREATE TABLE IF NOT EXISTS t_order_info (
    task_id VARCHAR(64) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    area VARCHAR(64),
    representative_office VARCHAR(64),
    country VARCHAR(64),
    accounts VARCHAR(64),
    project VARCHAR(64),
    po_id VARCHAR(255),
    PRIMARY KEY (task_id, order_id),
    CONSTRAINT fk_order_task FOREIGN KEY (task_id) REFERENCES t_profit_calculation_task(task_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
