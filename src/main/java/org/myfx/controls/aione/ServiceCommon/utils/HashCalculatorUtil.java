package org.myfx.controls.aione.ServiceCommon.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 哈希值计算工具类（专注于文件哈希值计算，支持多种算法）
 */
public class HashCalculatorUtil {

    /**
     * 支持的哈希算法常量（可根据需要扩展）
     */
    public static final String ALGORITHM_MD5 = "MD5";
    public static final String ALGORITHM_SHA1 = "SHA-1";
    public static final String ALGORITHM_SHA256 = "SHA-256";
    public static final String ALGORITHM_SHA512 = "SHA-512";

    /**
     * 计算MultipartFile的哈希值（默认使用SHA-256算法，更安全）
     * @param file 待计算的文件（Spring MVC上传的文件）
     * @return 哈希值的16进制字符串
     * @throws IOException 文件读取异常
     * @throws NoSuchAlgorithmException 不支持的算法异常
     */
    public static String calculateHash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        return calculateHash(file, ALGORITHM_SHA256);
    }

    /**
     * 计算MultipartFile的哈希值（指定算法）
     * @param file 待计算的文件（Spring MVC上传的文件）
     * @param algorithm 哈希算法（如ALGORITHM_SHA256）
     * @return 哈希值的16进制字符串
     * @throws IOException 文件读取异常
     * @throws NoSuchAlgorithmException 不支持的算法异常
     */
    public static String calculateHash(MultipartFile file, String algorithm) throws IOException, NoSuchAlgorithmException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        try (InputStream inputStream = file.getInputStream()) {
            return calculateHash(inputStream, algorithm);
        }
    }

    /**
     * 计算本地File的哈希值（默认使用SHA-256算法）
     * @param file 待计算的本地文件
     * @return 哈希值的16进制字符串
     * @throws IOException 文件读取异常
     * @throws NoSuchAlgorithmException 不支持的算法异常
     */
    public static String calculateHash(File file) throws IOException, NoSuchAlgorithmException {
        return calculateHash(file, ALGORITHM_SHA256);
    }

    /**
     * 计算本地File的哈希值（指定算法）
     * @param file 待计算的本地文件
     * @param algorithm 哈希算法（如ALGORITHM_MD5）
     * @return 哈希值的16进制字符串
     * @throws IOException 文件读取异常
     * @throws NoSuchAlgorithmException 不支持的算法异常
     */
    public static String calculateHash(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在或不是有效文件");
        }
        try (InputStream inputStream = new FileInputStream(file)) {
            return calculateHash(inputStream, algorithm);
        }
    }

    /**
     * 核心方法：通过输入流计算哈希值（底层实现，供以上方法调用）
     * @param inputStream 文件输入流（需外部保证关闭）
     * @param algorithm 哈希算法
     * @return 哈希值的16进制字符串
     * @throws IOException 流读取异常
     * @throws NoSuchAlgorithmException 算法不支持
     */
    private static String calculateHash(InputStream inputStream, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] buffer = new byte[8192]; // 8KB缓冲区，提升大文件处理效率
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, len);
        }
        // 将哈希字节数组转换为16进制字符串
        return bytesToHex(digest.digest());
    }

    /**
     * 字节数组转16进制字符串（辅助方法）
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : bytes) {
            // 转换为两位16进制，不足补0（如0x0a -> "0a"）
            hexBuilder.append(String.format("%02x", b));
        }
        return hexBuilder.toString();
    }
}
