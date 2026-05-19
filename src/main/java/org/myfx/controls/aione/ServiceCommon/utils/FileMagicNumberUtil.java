package org.myfx.controls.aione.ServiceCommon.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * 基于魔数的文件类型判断工具类（二进制数组版）
 * 支持：图片（JPG/PNG/GIF/BMP/WEBP）、音频（MP3/FLAC/WAV）、电子书（EPUB）
 */
public class FileMagicNumberUtil {

    // -------------------------- 魔数常量定义（不变） --------------------------
    // 图片类型魔数
    private static final byte[] MAGIC_JPG = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] MAGIC_PNG = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] MAGIC_GIF = new byte[]{0x47, 0x49, 0x46, 0x38}; // GIF87a/GIF89a
    private static final byte[] MAGIC_BMP = new byte[]{0x42, 0x4D}; // BM
    private static final byte[] MAGIC_WEBP = new byte[]{0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00, 0x57, 0x45, 0x42, 0x50};

    // 音频类型魔数
    private static final byte[] MAGIC_MP3_ID3 = new byte[]{0x49, 0x44, 0x33}; // ID3标签开头
    private static final byte[] MAGIC_FLAC = new byte[]{0x66, 0x4C, 0x61, 0x43}; // fLaC
    private static final byte[] MAGIC_WAV = new byte[]{0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00, 0x57, 0x41, 0x56, 0x45}; // RIFF + WAVE

    // 电子书类型魔数
    private static final byte[] MAGIC_EPUB = new byte[]{0x50, 0x4B, 0x03, 0x04}; // PK（EPUB是ZIP包）


    // -------------------------- 核心匹配方法（二进制数组版） --------------------------
    /**
     * 魔数匹配（支持前缀匹配，跳过魔数中0占位的可变字段）
     * @param data 文件二进制数据（至少包含魔数长度）
     * @param magic 目标魔数
     * @return true=匹配，false=不匹配
     */
    private static boolean matchMagic(byte[] data, byte[] magic) {
        if (data == null || data.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] == 0x00) continue; // 跳过可变长度占位符
            if (data[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }


    // -------------------------- 音频类型判断（按你的要求） --------------------------
    /**
     * 判断是否为音频文件（MP3/FLAC/WAV）
     * @param data 文件二进制数组（至少前12字节）
     * @return true=是音频，false=否
     */
    public static boolean isMusic(byte[] data) {
        return isMp3(data) || isFlac(data) || isWav(data);
    }

    /**
     * 获取音频文件的真实后缀名
     * @param data 文件二进制数组（至少前12字节）
     * @return 后缀名（如mp3/flac/wav），非音频返回null
     */
    public static String getRealMusicSuffix(byte[] data) {
        if (isMp3(data)) return "mp3";
        if (isFlac(data)) return "flac";
        if (isWav(data)) return "wav";
        return null;
    }

    // 细分音频类型判断（私有辅助）
    public static boolean isMp3(byte[] data) {
        return matchMagic(data, MAGIC_MP3_ID3);
    }

    public static boolean isFlac(byte[] data) {
        return matchMagic(data, MAGIC_FLAC);
    }

    public static boolean isWav(byte[] data) {
        return matchMagic(data, MAGIC_WAV);
    }


    // -------------------------- 图片类型判断（按你的要求） --------------------------
    /**
     * 判断是否为图片文件（JPG/PNG/GIF/BMP/WEBP）
     * @param data 文件二进制数组（至少前12字节）
     * @return true=是图片，false=否
     */
    public static boolean isImage(byte[] data) {
        return isJpg(data) || isPng(data) || isGif(data) || isBmp(data) || isWebp(data);
    }

    /**
     * 获取图片文件的真实后缀名
     * @param data 文件二进制数组（至少前12字节）
     * @return 后缀名（如jpg/png/gif/bmp/webp），非图片返回null
     */
    public static String getRealImageSuffix(byte[] data) {
        if (isJpg(data)) return "jpg";
        if (isPng(data)) return "png";
        if (isGif(data)) return "gif";
        if (isBmp(data)) return "bmp";
        if (isWebp(data)) return "webp";
        return null;
    }

    // 细分图片类型判断（私有辅助）
    public static boolean isJpg(byte[] data) {
        return matchMagic(data, MAGIC_JPG);
    }

    public static boolean isPng(byte[] data) {
        return matchMagic(data, MAGIC_PNG);
    }

    public static boolean isGif(byte[] data) {
        return matchMagic(data, MAGIC_GIF);
    }

    public static boolean isBmp(byte[] data) {
        return matchMagic(data, MAGIC_BMP);
    }

    public static boolean isWebp(byte[] data) {
        return matchMagic(data, MAGIC_WEBP);
    }


    // -------------------------- EPUB类型判断（按你的要求） --------------------------
    /**
     * 判断是否为EPUB电子书
     * @param data 文件二进制数组（至少前4字节）
     * @return true=是EPUB，false=否
     */
    public static boolean isEpub(byte[] data) {
        return matchMagic(data, MAGIC_EPUB);
    }


    // -------------------------- 工具方法：读取文件头字节（支持MultipartFile/File） --------------------------
    /**
     * 从输入流读取文件头字节（基础方法）
     * @param inputStream 输入流
     * @param length 读取长度（建议12）
     * @return 文件头字节数组
     */
    public static byte[] readFileHeader(InputStream inputStream, int length) throws IOException {
        byte[] header = new byte[length];
        int read = inputStream.read(header);
        return read < length ? Arrays.copyOf(header, read) : header;
    }

    /**
     * 从MultipartFile读取文件头字节（直接调用，无需手动处理流）
     * @param file Spring上传的MultipartFile
     * @param length 读取长度（建议12）
     * @return 文件头字节数组
     */
    public static byte[] readFileHeader(MultipartFile file, int length) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return readFileHeader(inputStream, length);
        }
    }

    /**
     * 从本地File读取文件头字节（直接调用，自动关闭流）
     * @param file 本地File对象
     * @param length 读取长度（建议12）
     * @return 文件头字节数组
     */
    public static byte[] readFileHeader(File file, int length) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return readFileHeader(inputStream, length);
        }
    }

    /**
     * 重载：默认读取前12字节（魔数判断足够）
     */
    public static byte[] readFileHeader(MultipartFile file) throws IOException {
        return readFileHeader(file, 12);
    }

    public static byte[] readFileHeader(File file) throws IOException {
        return readFileHeader(file, 12);
    }

    public static byte[] readFileHeader(InputStream inputStream) throws IOException {
        return readFileHeader(inputStream, 12);
    }
}