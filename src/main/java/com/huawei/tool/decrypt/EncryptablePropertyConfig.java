/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.voting.decrypt;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;

import io.micrometer.common.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EncryptablePropertyConfig
 *
 * @author w30023569
 * @since 2022/2/26
 */
@Configuration
class EncryptablePropertyConfig {
    private final static Logger logger = LoggerFactory.getLogger(EncryptablePropertyConfig.class);

    /**
     * EncryptablePropertyResolver
     *
     * @return encryptablePropertyResolver
     */
    @Bean(name = "encryptablePropertyResolver")
    public EncryptablePropertyResolver encryptablePropertyResolver() {
        return new EncryptionPropertyResolver();
    }

    static class EncryptionPropertyResolver implements EncryptablePropertyResolver {
        @Override
        public String resolvePropertyValue(String value) {
            if (StringUtils.isBlank(value)) {
                return value;
            }

            //值以AES@开头的均为AES加密，需要解密throws Exception
            if (value.startsWith("AES@")) {
                try {
                    return AES.getOriginalText(value.substring(4));
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }

            //不需要解密的值直接返回
            return value;
        }
    }
}
