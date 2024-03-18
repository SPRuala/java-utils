package com.bc.springboot.utils;


import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MinIOUtils {

   private static MinioClient minioClient;

   private static String endpoint;
   private static String bucketName;
   private static String accessKey;
   private static String secretkey;
   private static Integer imgSize;
   private static Integer fileSize;

   private static final String SEPARATOR = "/";

   public MinIOUtils() {
   }
   public MinIOUtils(String endpoint,String bucketName,String accessKey,String secretkey,Integer imgSize,Integer fileSize){
      MinIOUtils.endpoint=endpoint;
      MinIOUtils.bucketName=bucketName;
      MinIOUtils.accessKey=accessKey;
      MinIOUtils.secretkey=secretkey;
      MinIOUtils.imgSize=imgSize;
      MinIOUtils.fileSize=fileSize;
      createMinioClient();
   }
   //创建基于Java端的客户端(MinioClient)
   public void createMinioClient(){
      try {
         if (null ==minioClient){
            log.info("开始创建 MinioClient....");
            minioClient=MinioClient
                    .builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretkey)
                    .build();
            createBucket(bucketName); //没有该Bucket时创建一个Bucket,有则存在; bucketName为Config中所使用的属性
            log.info("创建完毕 MinioClient....");
         }
      }catch (Exception e){
         log.error("MinIO服务器异常:",e);
      }
   }

   //获取上传文件的前缀路径
   public static String getBasisUrl(){
      return endpoint+SEPARATOR+bucketName+SEPARATOR;
   }

   //_______________________________操作Bucket(桶)的相关api_______________________________________________________________

   /**
    * 启动SpringBoot容器的时候初始化Bucket
    * 如果没有Bucket则创建
    * @param bucketName bucketName
    * @throws Exception Exception
    */
   private static void createBucket(String bucketName) throws Exception {
      if (!bucketExists(bucketName)){
         minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
      }
   }

   /**
    * 判断Bucket是否存在
    * @param bucketName bucketName
    * @return true: 存在 false：不存在
    * @throws Exception Exception
    */
   public static boolean bucketExists(String bucketName) throws Exception{
      return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
   }

   /**
    * 获得Bucket策略
    * @param bucketName
    * @return
    * @throws Exception
    */
   public static String getBucketPolicy(String bucketName) throws Exception{
      String bucketPolicy = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
      return bucketPolicy;
   }

   /**
    * 获得Bucket列表
    * @return
    * @throws Exception
    */
   public static List<Bucket> getAllBuckets() throws Exception{
      return minioClient.listBuckets();
   }

   /**
    * 根据bucketName获取其相关信息
    * @param bucketName
    * @return
    * @throws Exception
    */
   public static Optional<Bucket> getBucket(String bucketName) throws Exception {
      return getAllBuckets().stream().filter(b ->b.name().equals(bucketName)).findFirst();
   }


   /**
    * 根据bucketName删除Bucket; true 删除成功,false 删除失败
    * @param bucketName
    * @throws Exception
    */
   public static void removeBucket(String bucketName) throws Exception {
      minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
   }

   /**
    * 判断文件是否存在
    * @param bucketName 存储桶
    * @param objectName 文件名
    * @return
    */
   public static boolean isObjectExist(String bucketName,String objectName){
      boolean exist=true;
      try {
         minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
      }catch (Exception e){
         exist=false;
      }
      return exist;
   }

   /**
    * 判断文件夹是否存在
    * @param bucketName 存储桶
    * @param objectName 文件夹名
    * @return
    */
   public static boolean isFolderExist(String bucketName,String objectName){
      boolean exist=false;
      try {
         Iterable<Result<Item>> results=minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(objectName).build());
         for (Result<Item> result :results){
            Item item=result.get();
            if (item.isDir() && objectName.equals(item.objectName())){
               exist=true;
            }
         }
      }catch (Exception e){
         exist=false;
      }
      return exist;
   }
   //3:14
   /**
   * 根据文件前缀查询文件
   * @param bucketName 存储桶
   * @param prefix 前缀
   * @param recursive 是否使用递归查询
   * @return MiniaItem 列表
   * @throws Exception
   */
   public static List<Item> getAll0bjectsByPrefix(String bucketName,String prefix,boolean recursive)throws Exception{
      List<Item> list = new ArrayList<>();
      Iterable<Result<Item>> objectsIterator = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(recursive).build());
      if(objectsIterator != null){
         for(Result<Item> o:objectsIterator){
            Item item=o.get();
            list.add(item);
         }
      }
      return list;
      }

   /**
   *获取文件流
   *@param bucketName 存储桶
   *@param objectName 文件名
    *@return 二进制流
    */
   public static InputStream getObject(String bucketName, String objectName) throws Exception{
      return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
   }

   /**
   *断点下载
   *@param bucketName 存桶
   *@param objectName 文件名称
   * @param offset 起始字节的位冒
   * @param length 要读取的长度
    * @return 二进制流
   */
   public InputStream getObject(String bucketName, String objectName, long offset, long length)throws Exception{
      return minioClient.getObject(GetObjectArgs
              .builder()
              .bucket(bucketName)
              .object(objectName)
              .offset(offset)
              .length(length)
              .build());
   }

   /**
   * 获取路径下文件列表
   * param bucketName 存储桶
   * @param prefix 文件名称
   *@param recursive 是否递归查找，false:模拟文件夹结构查找
   * @return 二进制流
   */
   public static Iterable<Result<Item>> listObjects(String bucketName,String prefix,boolean recursive){
      return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(recursive).build());
   }

   /**
    *使用MultipartFile进行文件上传
    * @param bucketName 存储桶
    * @param file 文件名
    * @param objectName 对象名
    * @param contentType 类型
    * return
    * @throws Exception
    */
   public static ObjectWriteResponse uploadFile(String bucketName, MultipartFile file, String objectName,String contentType)throws Exception{
      InputStream inputStream = file.getInputStream();
         return minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).contentType(contentType).stream(inputStream, inputStream.available(), -1).build());

   }
   /**
    * 上传本地文件
    * @param bucketName 存储桶
    * @param objectName 对象名称
    * @param fileName 本地文件路径
    */
   public static ObjectWriteResponse uploadFile(String bucketName, String objectName,String fileName)throws Exception{
      return minioClient.uploadObject(UploadObjectArgs.builder().bucket(bucketName).object(objectName).filename(fileName).build());
   }

   /**
    * 通过流上传文件
    * @param bucketName 存储桶
    * @param objectName 文件对象
    * @param inputStream 文件流
    */
   public static ObjectWriteResponse uploadFile(String bucketName, String objectName,InputStream inputStream)throws Exception{
      return minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(inputStream,inputStream.available(),-1).build());
   }

   /**
   * 创建文件夹或目录
   * @param bucketName 存储桶
   *@param objectName 目录路径
    */
   public static ObjectWriteResponse createDir(String bucketName, String objectName)throws Exception{
      return minioClient.putObject(
              PutObjectArgs.builder()
                      .bucket(bucketName)
                      .object(objectName)
                      .stream(new ByteArrayInputStream(new byte[]{}),0,0)
                      .build());
   }


   /**
   *获取文件信息，如果抛出异常则说明文件不存在
   * @param bucketName 存储桶
   * @param objectName 文件名称
   */
   public static String getFileStatusInfo(String bucketName, String objectName)throws Exception{
      return minioClient.statObject(
              StatObjectArgs.builder()
                      .bucket(bucketName)
                      .object(objectName)
                      .build())
              .toString();
   }

   /**
    * *拷贝文件
    * @param bucketName 存储桶
    * @param objectName 文件名
    * @param srcBucketName 目标存储桶
    * @param srcObjectName 目标文件名
    */
   public static ObjectWriteResponse copyFlle(String bucketName, String objectName,String srcBucketName,String srcObjectName)throws Exception{
      return minioClient.copyObject(
              CopyObjectArgs.builder()
              .source(CopySource.builder().bucket(bucketName).object(objectName).build())
              .bucket(srcBucketName)
              .object(srcObjectName)
              .build());
   }
   /**
    *删除文件
    *@param bucketName 存储桶
    *param obiectName 文件名称
    */
   public static void removefile(String bucketName, String objectName) throws Exception{
      RemoveObjectArgs.builder()
              .bucket(bucketName)
              .object(objectName)
              .build();
   }
   /**
   *批量删除文件
   * @param bucketName 存储桶
   * @param keys 需要测除的文件列表
   * @return
   */
   public static void removeFiles(String bucketName, List<String> keys) {
      List<DeleteObject> objects = new LinkedList<>();
      keys.forEach(s -> {
         objects.add(new DeleteObject(s));
         try {
            removefile(bucketName, s);
         } catch (Exception e) {
            log.error("批量删除失败lerror:", e);
         }
      });
   }
   /**
    * 获取文件外链
    *  @param bucketName 存储桶
    *  @param objectName 文件名
    *  @param expires 过期时间<=7 秒(外链有效时间(单位 秒))
    *  @return url
    *  @throws Exception
    */
   public static String getPresignedObjectUrl(String bucketName, String objectName,int expires)throws Exception{
      GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder().expiry(expires).bucket(bucketName).object(objectName).build();
      return minioClient.getPresignedObjectUrl(args);
   }

   /**
   *获得文件外链
   * @param bucketName 存储桶
   * @param objectName 文件名
   * @returh url
   * @throws Exception
   */
   public static String getRresianedObjectUrl(String bucketName, String objectName)throws Exception{
      GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder().bucket(bucketName).object(objectName).build();
      return minioClient.getPresignedObjectUrl(args);
   }

}


