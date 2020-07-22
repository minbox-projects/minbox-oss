package org.minbox.framework.oss.exception;

import lombok.NoArgsConstructor;

/**
 * Object storage exception
 * extend from {@link RuntimeException}
 *
 * @author 恒宇少年
 */
@NoArgsConstructor
public class ObjectStorageException extends RuntimeException {
    public ObjectStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
