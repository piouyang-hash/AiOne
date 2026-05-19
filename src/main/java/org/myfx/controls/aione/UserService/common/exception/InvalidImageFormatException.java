package org.myfx.controls.aione.UserService.common.exception;

/**
 * 自定义异常：当文件不是有效图片格式时抛出
 */
public class InvalidImageFormatException extends RuntimeException {

    // 带消息的构造方法
    public InvalidImageFormatException(String message) {
        super(message);
    }
}