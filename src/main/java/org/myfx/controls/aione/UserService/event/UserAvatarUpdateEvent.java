package org.myfx.controls.aione.UserService.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户更新头像事件
 */
@Getter
public class UserAvatarUpdateEvent extends ApplicationEvent {

    // getter方法（新增avatarUrl的getter）
    private final MultipartFile file;      // 上传的头像文件
    private final Integer userId;         // 用户ID
    // 添加set方法
    @Setter
    private String avatarUrl;       // 新增：头像存储路径/URL

    // 构造方法
    public UserAvatarUpdateEvent(Object source, MultipartFile file, Integer userId) {
        super(source);
        this.file = file;
        this.userId = userId;
    }

}
