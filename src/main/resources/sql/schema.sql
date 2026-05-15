-- Full DDL (multiple statements).
-- DBeaver: use "Execute SQL Script" (Alt+X / Execute SQL Script), NOT "Execute SQL Statement" (Ctrl+Enter).
-- MySQL CLI: mysql -u USER -p DATABASE < schema.sql
-- If your client still fails, run files under sql/install/ in numeric order (one CREATE per file).

CREATE TABLE IF NOT EXISTS t_product_cost_price (
    device_id VARCHAR(64) NOT NULL PRIMARY KEY,
    description TEXT,
    device_type VARCHAR(64),
    currency VARCHAR(32),
    psp_m01_cost DECIMAL(12,2), psp_m02_cost DECIMAL(12,2), psp_m03_cost DECIMAL(12,2), psp_m04_cost DECIMAL(12,2),
    psp_m05_cost DECIMAL(12,2), psp_m06_cost DECIMAL(12,2), psp_m07_cost DECIMAL(12,2), psp_m08_cost DECIMAL(12,2),
    psp_m09_cost DECIMAL(12,2), psp_m10_cost DECIMAL(12,2), psp_m11_cost DECIMAL(12,2), psp_m12_cost DECIMAL(12,2),
    std_m01_cost DECIMAL(12,2), std_m02_cost DECIMAL(12,2), std_m03_cost DECIMAL(12,2), std_m04_cost DECIMAL(12,2),
    std_m05_cost DECIMAL(12,2), std_m06_cost DECIMAL(12,2), std_m07_cost DECIMAL(12,2), std_m08_cost DECIMAL(12,2),
    std_m09_cost DECIMAL(12,2), std_m10_cost DECIMAL(12,2), std_m11_cost DECIMAL(12,2), std_m12_cost DECIMAL(12,2),
    addition_info TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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

CREATE TABLE IF NOT EXISTS t_order_info (
    task_id VARCHAR(64) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    area VARCHAR(64),
    representative_office VARCHAR(64),
    country VARCHAR(64),
    accounts VARCHAR(64),
    project VARCHAR(64),
    po_id VARCHAR(255),
    product_domain VARCHAR(64),
    PRIMARY KEY (task_id, order_id),
    CONSTRAINT fk_order_task FOREIGN KEY (task_id) REFERENCES t_profit_calculation_task(task_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_order_product_detail_data (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    device_type VARCHAR(64),
    device_id VARCHAR(64),
    description TEXT,
    category_type VARCHAR(32),
    hardware_type VARCHAR(32),
    scene TEXT,
    device_count INT,
    income_month VARCHAR(32),
    currency VARCHAR(32),
    list_price DECIMAL(12,2),
    before_tier DECIMAL(12,2),
    before_price DECIMAL(12,2),
    discount_desc_before TEXT,
    after_tier DECIMAL(12,2),
    after_price DECIMAL(12,2),
    discount_desc_after TEXT,
    tier_increase DECIMAL(12,2),
    price_increase DECIMAL(12,2),
    after_tier_discount_rate DECIMAL(7,4),
    after_price_discount_rate DECIMAL(7,4),
    before_total_price DECIMAL(12,2),
    after_total_price DECIMAL(12,2),
    total_price_increase DECIMAL(12,2),
    total_increase_rate DECIMAL(7,4),
    gross_profit DECIMAL(12,2),
    addition_info TEXT,
    CONSTRAINT fk_detail_task FOREIGN KEY (task_id) REFERENCES t_profit_calculation_task(task_id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_order FOREIGN KEY (task_id, order_id) REFERENCES t_order_info(task_id, order_id) ON DELETE CASCADE,
    KEY idx_detail_task (task_id),
    KEY idx_detail_order (task_id, order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_summary_report (
    task_id VARCHAR(64) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    income_month VARCHAR(64) NOT NULL,
    area VARCHAR(64),
    representative_office VARCHAR(64),
    country VARCHAR(64),
    accounts VARCHAR(64),
    project VARCHAR(64),
    po_id VARCHAR(255),
    product_domain VARCHAR(64),
    gross_profit DECIMAL(12,2),
    hw_psp_gross_profit DECIMAL(7,4),
    hw_standard_gross_profit DECIMAL(7,4),
    before_total_price DECIMAL(12,2),
    after_total_price DECIMAL(12,2),
    total_price_increase DECIMAL(12,2),
    price_increase_rate DECIMAL(7,4),
    software_history_price DECIMAL(12,2),
    software_price DECIMAL(12,2),
    software_price_increase_rate DECIMAL(7,4),
    PRIMARY KEY (task_id, order_id),
    CONSTRAINT fk_summary_task FOREIGN KEY (task_id) REFERENCES t_profit_calculation_task(task_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
