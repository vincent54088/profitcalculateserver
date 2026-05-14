package com.huawei.tool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 上传文件存储目录（Maven 过滤后可为绝对路径）
     */
    private String uploadDir = "target/uploads";

    /**
     * 始终返回绝对路径，避免 Tomcat 对 {@code MultipartFile.transferTo} 将相对路径解析到
     * servlet 工作目录，而 {@code Files.createDirectories} 却按 {@code user.dir} 创建目录的不一致。
     */
    public String getUploadDir() {
        return Paths.get(uploadDir).toAbsolutePath().normalize().toString();
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
