package com.hjm.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "pss.alioss")
public class AliOSSProperties {
    private String endpoint ;
    private String accessKeyId;
    private String accessKeySecret ;
    private String bucketName;
}
