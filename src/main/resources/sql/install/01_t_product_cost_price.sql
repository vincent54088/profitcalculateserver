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
