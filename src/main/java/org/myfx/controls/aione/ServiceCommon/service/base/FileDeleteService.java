package org.myfx.controls.aione.ServiceCommon.service.base;

/**
 * 文件删除服务
 */
public interface FileDeleteService {

    /**
     * 根据文件路径删除单个文件
     * @param filePath 文件绝对路径
     */
    void deleteFile(String filePath);

}