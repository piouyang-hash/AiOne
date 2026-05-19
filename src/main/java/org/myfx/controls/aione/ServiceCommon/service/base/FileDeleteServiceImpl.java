package org.myfx.controls.aione.ServiceCommon.service.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 文件删除服务实现类
 */
@Slf4j
@Service
public class FileDeleteServiceImpl implements FileDeleteService {

    @Override
    public void deleteFile(String filePath) {
        // 1. 校验路径不能为空
        if (filePath == null || filePath.isBlank()) {
            throw new RuntimeException("文件删除失败：文件路径不能为空");
        }

        File file = new File(filePath);

        try {
            // 2. 判断文件是否存在
            if (!file.exists()) {
                log.warn("文件删除失败：文件不存在，路径：{}", filePath);
                throw new RuntimeException("文件删除失败：文件不存在");
            }

            // 3. 判断是否为文件（防止误删文件夹）
            if (!file.isFile()) {
                log.error("文件删除失败：路径指向的不是文件，路径：{}", filePath);
                throw new RuntimeException("文件删除失败：该路径不是有效文件");
            }

            // 4. 执行删除
            boolean deleteSuccess = file.delete();

            if (!deleteSuccess) {
                throw new RuntimeException("文件删除失败：系统无法删除该文件");
            }

            log.info("文件删除成功，路径：{}", filePath);

        } catch (Exception e) {
            log.error("文件删除异常，路径：{}，异常信息：{}", filePath, e.getMessage());
            // 捕获所有异常，统一抛出运行时异常
            throw new RuntimeException("文件删除失败：" + e.getMessage());
        }
    }
}
