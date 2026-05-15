/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.voting.decrypt;

import lombok.Data;

/**
 * AES算法所需的key和iv值
 *
 * @author w30022769
 * @since 2024-01-18
 */
@Data
public class AESKey {
    private String key;

    private String iv;

    public AESKey(String key, String iv) {
        this.key = key;
        this.iv = iv;
    }

    /**
     * 判空
     *
     * @return true:空; false 非空
     */
    public boolean isEmpty() {
        if (this.key.isEmpty() || this.iv.isEmpty()) {
            return true;
        }
        return false;
    }
}
