package org.myfx.controls.aione.ServiceCommon.exception;

/**
 * 文件大小超出限制异常
 */
public class FileSizeExceededException extends RuntimeException {

    public FileSizeExceededException(String message) {
        super(message);
    }
}
