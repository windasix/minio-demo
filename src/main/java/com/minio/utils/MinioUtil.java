package com.minio.utils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.minio.config.MinioProp;
import com.minio.dto.response.FileUploadResponse;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Component
public class MinioUtil {

    @Autowired
    private MinioProp minioProp;

    @Autowired
    private MinioClient client;

    /**
     * 创建bucket
     */
    public void createBucket(String bucketName) throws Exception {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 上传文件
     */
    public FileUploadResponse uploadFile(MultipartFile file, String bucketName) throws Exception {
        //判断文件是否为空
        if (null == file || 0 == file.getSize()) {
            return null;
        }
        //判断存储桶是否存在  不存在则创建
        createBucket(bucketName);
        //文件名
        String originalFilename = file.getOriginalFilename();
        //新的文件名 = 存储桶文件名_时间戳.后缀名
        assert originalFilename != null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = bucketName + "_" +
                System.currentTimeMillis() + "_" + format.format(new Date()) + "_" + new Random().nextInt(1000) +
                originalFilename.substring(originalFilename.lastIndexOf("."));
        //开始上传
        client.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        String url = minioProp.getEndpoint() + "/" + bucketName + "/" + fileName;
        return new FileUploadResponse(url);
    }


    public FileUploadResponse uploadLargeFile(MultipartFile file, String bucketName, Integer partCount) throws Exception {
        //判断文件是否为空
        if (null == file || 0 == file.getSize()) {
            return null;
        }
        //判断存储桶是否存在  不存在则创建
        createBucket(bucketName);
        //文件名
        String originalFilename = file.getOriginalFilename();
        //新的文件名 = 存储桶文件名_时间戳.后缀名
        assert originalFilename != null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = bucketName + "_" +
            System.currentTimeMillis() + "_" + format.format(new Date()) + "_" + new Random().nextInt(1000) +
            originalFilename.substring(originalFilename.lastIndexOf("."));
        AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials(minioProp.getAccessKey(), minioProp.getSecretKey()));
        s3.setEndpoint(minioProp.getEndpoint());
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, fileName);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        s3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), objectMetadata)
            .withGeneralProgressListener(new ProgressListener(){
                int readedbyte = 0;
                @Override
                public void progressChanged(ProgressEvent progressEvent) {
                    readedbyte += progressEvent.getBytesTransferred();
                    System.out.println("progress：" + decimalFormat.format (readedbyte / (float)file.getSize()));
                }
            }));
        URL url2 = s3.generatePresignedUrl(urlRequest);
        System.out.println("URL2：" + minioProp.getEndpoint() + url2.getPath());
        String url = minioProp.getEndpoint() + "/" + bucketName + "/" + fileName;
//        log.info("上传文件成功url ：[{}], urlHost ：[{}]", url, urlHost);
        return new FileUploadResponse(url);
    }

    /**
     * 获得状态
     */
    public void getStatus() throws Exception {

//        String sqlExpression = "select * from S3Object";
//        InputSerialization is = new InputSerialization(null, false, null, null, FileHeaderInfo.USE, null, null, null);
//        OutputSerialization os = new OutputSerialization(null, null, null, QuoteFields.ASNEEDED, null);
//        SelectResponseStream stream =
//            client.selectObjectContent(
//                SelectObjectContentArgs.builder()
//                    .bucket("livedingding")
//                    .object("livedingding_1623743860781_2021-06-15_293.png")
//                    .sqlExpression(sqlExpression)
//                    .inputSerialization(is)
//                    .outputSerialization(os)
//                    .requestProgress(true)
//                    .build());
//
//        byte[] buf = new byte[512];
//        int bytesRead = stream.read(buf, 0, buf.length);
//        System.out.println(new String(buf, 0, bytesRead, StandardCharsets.UTF_8));
//        Stats stats = stream.stats();
//        System.out.println("bytes scanned: " + stats.bytesScanned());
//        System.out.println("bytes processed: " + stats.bytesProcessed());
//        System.out.println("bytes returned: " + stats.bytesReturned());
//        stream.close();

//        AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials(minioProp.getAccessKey(), minioProp.getSecretKey()));
//        s3.setEndpoint(minioProp.getEndpoint());
//        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
//            "oss", "test-xzy/myS3File.mp4");
//        //这里我们上传文件的时候bucketName就需要替换为oss，bucketName拼到文件名的前面
//        s3.putObject(new PutObjectRequest("oss","test-xzy/myS3File.mp4",file).withGeneralProgressListener(new ProgressListener(){
//            int readedbyte = 0;
//            @Override
//            public void progressChanged(ProgressEvent progressEvent) {
//                readedbyte += progressEvent.getBytesTransferred();
//                System.out.println("=========progress=================" + (readedbyte / (float)file.length()) + "============URL=============");
//            }
//        }));
//        URL url = s3.generatePresignedUrl(urlRequest);
//        System.out.println("=========URL=================" + url.toString() + "============URL=============");

    }



    /**
     * 获取全部bucket
     *
     * @return
     */
    public List<Bucket> getAllBuckets() throws Exception {
        return client.listBuckets();
    }

    /**
     * 根据bucketName获取信息
     *
     * @param bucketName bucket名称
     */
    public Optional<Bucket> getBucket(String bucketName) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidResponseException, InternalException, ErrorResponseException, ServerException, XmlParserException {
        return client.listBuckets().stream().filter(b -> b.name().equals(bucketName)).findFirst();
    }

    /**
     * 根据bucketName删除信息
     *
     * @param bucketName bucket名称
     */
    public void removeBucket(String bucketName) throws Exception {
        client.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     * 获取⽂件外链
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @param expires    过期时间 <=7
     * @return url
     */
    public String getObjectURL(String bucketName, String objectName, Integer expires) throws Exception {
        return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucketName).object(objectName).expiry(expires).build());
    }

    /**
     * 获取⽂件
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @return ⼆进制流
     */
    public InputStream getObject(String bucketName, String objectName) throws Exception {
        return client.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 上传⽂件
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @param stream     ⽂件流
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#putObject
     */
    public void putObject(String bucketName, String objectName, InputStream stream) throws
            Exception {
        client.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(stream, stream.available(), -1).contentType(objectName.substring(objectName.lastIndexOf("."))).build());
    }

    /**
     * 上传⽂件
     *
     * @param bucketName  bucket名称
     * @param objectName  ⽂件名称
     * @param stream      ⽂件流
     * @param size        ⼤⼩
     * @param contextType 类型
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#putObject
     */
    public void putObject(String bucketName, String objectName, InputStream stream, long
            size, String contextType) throws Exception {
        client.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(stream, size, -1).contentType(contextType).build());
    }

    /**
     * 获取⽂件信息
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#statObject
     */
    public StatObjectResponse getObjectInfo(String bucketName, String objectName) throws Exception {
        return client.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 删除⽂件
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @throws Exception https://docs.minio.io/cn/java-client-apireference.html#removeObject
     */
    public void removeObject(String bucketName, String objectName) throws Exception {
        client.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }
}
