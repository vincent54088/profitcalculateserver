CREATE TABLE IF NOT EXISTS t_profit_calculation_task (
    task_id VARCHAR(64) NOT NULL PRIMARY KEY,
    task_name VARCHAR(255),
    task_status VARCHAR(32) NOT NULL,
    data_file VARCHAR(255),
    file_name VARCHAR(512),
    create_time DATETIME NOT NULL,
    update_time DATETIME,
    finish_time DATETIME,
    fail_reason TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
