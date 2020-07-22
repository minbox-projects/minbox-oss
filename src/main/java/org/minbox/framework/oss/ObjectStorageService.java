package org.minbox.framework.oss;



import org.minbox.framework.oss.exception.ObjectStorageException;

import java.io.InputStream;

/**
 * ApiBoot Object Storage Interface Definition
 *
 * @author 恒宇少年
 */
public interface ObjectStorageService {
    /**
     * byte array upload file
     *
     * @param objectName file name
     * @param bytes      file byte array
     * @return {@link ObjectStorageResponse}
     * @throws ObjectStorageException object storage exception
     */
    ObjectStorageResponse upload(String objectName, byte[] bytes) throws ObjectStorageException;

    /**
     * input stream upload file
     *
     * @param objectName  file name
     * @param inputStream file input stream
     * @return {@link ObjectStorageResponse}
     * @throws ObjectStorageException object storage exception
     */
    ObjectStorageResponse upload(String objectName, InputStream inputStream) throws ObjectStorageException;

    /**
     * local path upload file
     *
     * @param objectName file name
     * @param localFile  file local path
     * @return {@link ObjectStorageResponse}
     * @throws ObjectStorageException object storage exception
     */
    ObjectStorageResponse upload(String objectName, String localFile) throws ObjectStorageException;

    /**
     * download file
     *
     * @param objectName file name in the object store
     * @param localFile  file local path
     * @throws ObjectStorageException object storage exception
     */
    void download(String objectName, String localFile) throws ObjectStorageException;

    /**
     * delete file
     *
     * @param objectName file name in the object store
     * @throws ObjectStorageException object storage exception
     */
    void delete(String objectName) throws ObjectStorageException;
}
