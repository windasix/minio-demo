package com.minio.controller;

import com.minio.dto.response.FileUploadResponse;
import com.minio.utils.MinioUtil;
import io.minio.errors.MinioException;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Slf4j
@RestController
@RequestMapping("/file")
public class MinioController {

    @Autowired
    private MinioUtil minioUtil;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public FileUploadResponse uploadFile(@RequestParam(name = "file", required = false) MultipartFile file,
                                     @RequestParam(required = false, defaultValue = "salt") String bucketName) {
        FileUploadResponse response = null;
        try {
            response = minioUtil.uploadFile(file, bucketName);
        } catch (Exception e) {
//            log.error("上传失败 : [{}]", Arrays.asList(e.getStackTrace()));
            log.error("上传失败", e);
        }
        return response;
    }

    @ApiOperation("大文件分片上传")
    @RequestMapping(value = "/upload/large", method = RequestMethod.POST)
    @ResponseBody
    public FileUploadResponse uploadLargeFile(@RequestParam(name = "file", required = false) MultipartFile file,
                                              @RequestParam(required = false, defaultValue = "salt") String bucketName,
                                              @RequestParam(required = false, defaultValue = "10") Integer partCount) {
        FileUploadResponse response = null;
        try {
            response = minioUtil.uploadLargeFile(file, bucketName, partCount);
        } catch (Exception e) {
//            log.error("上传失败 : [{}]", Arrays.asList(e.getStackTrace()));
            log.error("上传失败：{},", e);
        }
        return response;
    }

    /**
     * 删除文件
     */
    @PostMapping("/delete")
    public void delete(@RequestParam(name="objectName", required = true) String objectName,
                       @RequestParam(required = false, defaultValue = "salt") String bucketName) throws Exception {
        minioUtil.removeObject(bucketName, objectName);
        log.error("删除成功");
    }

    /**
     * 下载文件到本地
     */
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadToLocal(@RequestParam(name="objectName", required = true) String objectName,
                                                  @RequestParam(required = false, defaultValue = "salt") String bucketName,
                                                  HttpServletResponse response) throws Exception {
        ResponseEntity<byte[]> responseEntity = null;
        InputStream stream = null;
        ByteArrayOutputStream output = null;
        try {
            // 获取"myobject"的输入流。
            stream = minioUtil.getObject(bucketName, objectName);
            if (stream == null) {
                log.error("文件不存在");
                throw new RuntimeException("文件不存在");
            }
            //用于转换byte
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = stream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            byte[] bytes = output.toByteArray();
            //设置header
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Accept-Ranges", "bytes");
            httpHeaders.add("Content-Length", bytes.length + "");
//            objectName = new String(objectName.getBytes("UTF-8"), "ISO8859-1");
            //把文件名按UTF-8取出并按ISO8859-1编码，保证弹出窗口中的文件名中文不乱码，中文不要太多，最多支持17个中文，因为header有150个字节限制。
            httpHeaders.add("Content-disposition", "attachment; filename=" + objectName);
            httpHeaders.add("Content-Type", "text/plain;charset=utf-8");
//            httpHeaders.add("Content-Type", "image/jpeg");
            responseEntity = new ResponseEntity<>(bytes, httpHeaders, HttpStatus.CREATED);
        } catch (MinioException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (output != null) {
                output.close();
            }
        }
        return responseEntity;
    }

    /**
     * 在浏览器预览图片
     */
    @GetMapping("/preViewPicture/{objectName}")
    public void preViewPicture(@PathVariable("objectName") String objectName, HttpServletResponse response) throws Exception {
        response.setContentType("image/jpeg");
        try (ServletOutputStream out = response.getOutputStream()) {
            InputStream stream = minioUtil.getObject("salt", objectName);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = stream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            byte[] bytes = output.toByteArray();
            out.write(bytes);
            out.flush();
        }
    }

    @PostMapping("/test")
    public void getTest() throws Exception {

        minioUtil.getStatus();

    }



}
