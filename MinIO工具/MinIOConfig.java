package com.bc.springboot.config;

import com.bc.springboot.utils.MinIOUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//MinIO配置类

@Configuration //把该类作为配置放入到Spring容器中
@Data
public class MinIOConfig {
    //以下属性需要在yaml文件中配置并初始化
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.fileHost}")
    private String fileHost;
    @Value("${minio.bucketName}")
    private String bucketName;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.imgSize}")
    private Integer imgSize;
    @Value("${minio.fileSize}")
    private Integer fileSize;

    //Bean在容器初始化时进行构建
        //容器初始化时创建工具类, 并传入相应属性
    @Bean
    public MinIOUtils creatMinioClient(){
        return new MinIOUtils(endpoint,bucketName,accessKey,secretKey,imgSize,fileSize);
    }
}
