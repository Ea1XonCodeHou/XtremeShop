package com.eaxon.xtreme_common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.eaxon.xtreme_common.properties.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 阿里云 OSS 工具类
 * - uploadFile：上传文件，返回公网访问 URL
 * - deleteFile：按公网 URL 删除 OSS 文件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AliOssUtil {

    private final OssProperties ossProperties;

    /**
     * 上传文件到 OSS
     *
     * @param fileName    存储的文件名（含路径前缀，如 "products/abc.jpg"）
     * @param inputStream 文件输入流
     * @return 文件公网访问 URL
     */
    public String uploadFile(String fileName, InputStream inputStream) {
        OSS ossClient = buildClient();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentDisposition("inline");
            ossClient.putObject(ossProperties.getBucketName(), fileName, inputStream, metadata);

            String url = buildUrl(fileName);
            log.info("OSS 上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("OSS 上传失败, fileName={}", fileName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 按公网 URL 删除 OSS 文件
     *
     * @param fileUrl 文件公网 URL（uploadFile 返回的值）
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        // 从 URL 中提取 objectKey：去掉 "https://bucket.endpoint/" 前缀
        String prefix = "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/";
        if (!fileUrl.startsWith(prefix)) {
            log.warn("OSS 删除跳过，URL 不匹配当前 bucket: {}", fileUrl);
            return;
        }
        String objectKey = fileUrl.substring(prefix.length());

        OSS ossClient = buildClient();
        try {
            ossClient.deleteObject(ossProperties.getBucketName(), objectKey);
            log.info("OSS 删除成功: {}", objectKey);
        } catch (Exception e) {
            log.error("OSS 删除失败, objectKey={}", objectKey, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }

    // ---- 私有方法 ----

    private OSS buildClient() {
        return new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
    }

    private String buildUrl(String fileName) {
        return "https://" + ossProperties.getBucketName()
                + "." + ossProperties.getEndpoint()
                + "/" + fileName;
    }
}
