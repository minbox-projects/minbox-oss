package org.minbox.framework.oss.support.aliyun;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.*;
import lombok.Setter;
import org.minbox.framework.oss.ObjectStorageProgress;
import org.minbox.framework.oss.ObjectStorageResponse;
import org.minbox.framework.oss.ObjectStorageService;
import org.minbox.framework.oss.PartSize;
import org.minbox.framework.oss.exception.ObjectStorageException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Ospi file operation class provided by ApiBoot
 *
 * @author 恒宇少年
 */
public class AliyunObjectStorageService implements ObjectStorageService {
    /**
     * region endpoint
     */
    protected String endpoint;
    /**
     * Storage name
     */
    protected String bucketName;
    /**
     * Alibaba Cloud account authorization id
     */
    protected String accessKeyId;
    /**
     * Alibaba Cloud account authorization secret
     */
    protected String accessKeySecret;
    /**
     * customer domain name
     */
    protected String domain;
    /**
     * ApiBoot Oss Progress
     */
    @Setter
    private ObjectStorageProgress objectStorageProgress;

    /**
     * Initialize global variables using constructor
     *
     * @param endpoint        {@link #endpoint}
     * @param bucketName      {@link #bucketName}
     * @param accessKeyId     {@link #accessKeyId}
     * @param accessKeySecret {@link #accessKeySecret}
     * @param domain          {@link #domain}
     */
    public AliyunObjectStorageService(String endpoint, String bucketName, String accessKeyId, String accessKeySecret, String domain) {
        this.endpoint = endpoint;
        this.bucketName = bucketName;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.domain = domain;
    }

    @Override
    public ObjectStorageResponse upload(String objectName, byte[] bytes) throws ObjectStorageException {
        try {
            OSSClient ossClient = getOssClient();
            // put byte inputStream
            ossClient.putObject(new PutObjectRequest(bucketName, objectName, new ByteArrayInputStream(bytes)).withProgressListener(new AliyunProgressListener(objectName, objectStorageProgress)));
            closeOssClient(ossClient);
        } catch (Exception e) {
            throw new ObjectStorageException(e.getMessage(), e);
        }
        return ObjectStorageResponse.builder().objectName(objectName).objectUrl(getObjectUrl(objectName)).build();
    }

    @Override
    public ObjectStorageResponse upload(String objectName, InputStream inputStream) throws ObjectStorageException {
        try {
            OSSClient ossClient = getOssClient();
            // put byte inputStream
            ossClient.putObject(new PutObjectRequest(bucketName, objectName, inputStream).withProgressListener(new AliyunProgressListener(objectName, objectStorageProgress)));
            closeOssClient(ossClient);
        } catch (Exception e) {
            throw new ObjectStorageException(e.getMessage(), e);
        }
        return ObjectStorageResponse.builder().objectName(objectName).objectUrl(getObjectUrl(objectName)).build();
    }

    @Override
    public ObjectStorageResponse upload(String objectName, String localFile) throws ObjectStorageException {
        try {
            OSSClient ossClient = getOssClient();
            // put byte inputStream
            ossClient.putObject(new PutObjectRequest(bucketName, objectName, new File(localFile)).withProgressListener(new AliyunProgressListener(objectName, objectStorageProgress)));
            closeOssClient(ossClient);
        } catch (Exception e) {
            throw new ObjectStorageException(e.getMessage(), e);
        }
        return ObjectStorageResponse.builder().objectName(objectName).objectUrl(getObjectUrl(objectName)).build();
    }

    @Override
    public void download(String objectName, String localFile) throws ObjectStorageException {
        try {
            OSSClient ossClient = getOssClient();
            ossClient.getObject(new GetObjectRequest(bucketName, objectName).withProgressListener(new AliyunProgressListener(objectName, objectStorageProgress)), new File(localFile));
            closeOssClient(ossClient);
        } catch (Exception e) {
            throw new ObjectStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String objectName) throws ObjectStorageException {
        try {
            OSSClient ossClient = getOssClient();
            ossClient.deleteObject(bucketName, objectName);
            closeOssClient(ossClient);
        } catch (Exception e) {
            throw new ObjectStorageException(e.getMessage(), e);
        }
    }

    /**
     * multi part upload file
     * with local file
     *
     * @param objectName object name
     * @param uploadFile upload file
     * @param partSize   every part size
     * @return ApiBootObjectStorageResponse
     * @throws ObjectStorageException ApiBoot Oss Exception
     */
    public ObjectStorageResponse multipartUpload(String objectName, File uploadFile, long partSize) throws ObjectStorageException {
        try {
            OSSClient ossClient = getOssClient();

            // init multi part upload request
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);

            // get upload id
            InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);
            String uploadId = result.getUploadId();
            List<PartETag> partETags = new ArrayList();
            // local file length
            long fileLength = uploadFile.length();
            // part count
            int partCount = (int) (fileLength / partSize);

            if (fileLength % partSize != 0) {
                partCount++;
            }

            for (int i = 0; i < partCount; i++) {
                // start position
                long startPos = i * partSize;
                // current part size
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;

                InputStream is = new FileInputStream(uploadFile);
                is.skip(startPos);

                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(objectName);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(is);
                // set part size
                uploadPartRequest.setPartSize(curPartSize);
                // set part number
                uploadPartRequest.setPartNumber(i + 1);

                // execute upload part
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                partETags.add(uploadPartResult.getPartETag());
            }

            // sort by part number
            Collections.sort(partETags, Comparator.comparingInt(PartETag::getPartNumber));

            // merge upload part file
            CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);
            ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            closeOssClient(ossClient);
        } catch (Exception e) {
            throw new ObjectStorageException(e.getMessage(), e);
        }
        return ObjectStorageResponse.builder().objectName(objectName).objectUrl(getObjectUrl(objectName)).build();
    }

    /**
     * multi part upload file
     * with local file string path
     *
     * @param objectName object name
     * @param localFile  local file
     * @param partSize   every part size
     * @return ApiBootObjectStorageResponse
     * @throws ObjectStorageException ApiBoot Oss Exception
     * @see PartSize
     */
    public ObjectStorageResponse multipartUpload(String objectName, String localFile, long partSize) throws ObjectStorageException {
        // load local file
        File uploadFile = new File(localFile);
        // execute multi part upload file
        return multipartUpload(objectName, uploadFile, partSize);
    }

    /**
     * get oss client instance
     *
     * @return {@link OSSClient}
     * @throws ObjectStorageException ApiBoot Oss Exception
     */
    protected OSSClient getOssClient() throws ObjectStorageException {
        try {
            OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
            return ossClient;
        } catch (Exception e) {
            throw new ObjectStorageException("获取OssClient对象异常.", e);
        }
    }

    /**
     * close given oss client instance
     *
     * @param ossClient {@link OSSClient}
     * @throws ObjectStorageException 对象存储异常对象
     */
    protected void closeOssClient(OSSClient ossClient) throws ObjectStorageException {
        ossClient.shutdown();
    }

    /**
     * get the default file address
     * Use Alibaba Cloud endpoint external network address for combination
     *
     * @param objectName file name
     * @return the default object url
     */
    protected String getDefaultObjectUrl(String objectName) {
        return String.format("https://%s.%s/%s", bucketName, endpoint.replace("http://", ""), objectName);
    }

    /**
     * get upload file path
     * If the address is configured, use the custom configuration address, otherwise use the default address
     *
     * @param objectName file name
     * @return file access address
     */
    protected String getObjectUrl(String objectName) {
        if (domain != null && domain.length() > 0) {
            return String.format(domain + "/%s", objectName);
        }
        return getDefaultObjectUrl(objectName);
    }


}
