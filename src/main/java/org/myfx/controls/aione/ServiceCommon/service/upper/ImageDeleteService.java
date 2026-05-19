package org.myfx.controls.aione.ServiceCommon.service.upper;

/**
 * 图片删除业务接口
 * 对应上传业务：AI角色头像 / 用户头像 删除
 */
public interface ImageDeleteService {

    /**
     * 删除 AI 角色头像
     * @param filePath 文件绝对路径
     * @param userId 操作人ID
     */
    void deleteAiRoleAvatar(String filePath, Integer userId);

    /**
     * 删除 用户头像
     * @param filePath 文件绝对路径
     * @param userId 操作人ID
     */
    void deleteUserAvatar(String filePath, Integer userId);

}