package org.minbox.framework.oss;

import lombok.Builder;
import lombok.Data;

/**
 * ApiBoot object storage response entity
 *
 * @author 恒宇少年
 */
@Data
@Builder
public class ObjectStorageResponse {
    /**
     * file name
     */
    private String objectName;
    /**
     * file request url
     */
    private String objectUrl;
}
