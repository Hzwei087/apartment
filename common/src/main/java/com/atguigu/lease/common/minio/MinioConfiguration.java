package com.atguigu.lease.common.minio;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@ConditionalOnProperty(name = "minio.endpoint")
//该注解表达的含义是只有当`minio.endpoint`属性存在时，该配置类才会生效。由于common模块中配置了MinioClient这个Bean，
// 并且web-app模块依赖于common模块，因此在启动AppWebApplication时，SpringBoot会创建一个MinioClient实例，
// 但是由于web-app模块的application.yml文件中并未提供MinioClient所需的参数（web-app模块暂时不需要使用MinioClient），
// 因此MinioClient实例的创建会失败。
public class MinioConfiguration {
    @Autowired
    private MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}
