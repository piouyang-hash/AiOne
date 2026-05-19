package org.myfx.controls.aione.UserService.common;

/**
 * 意见反馈常量类（统一管理状态/类型，前后端对齐）
 * 备注：feedback_type的值与前端<option>的value完全一致，前端可直接对接
 */
public class FeedBackConstants {

    // ====================== 反馈处理状态（对应表中status字段，INTEGER类型） ======================
    /** 处理状态：未处理（默认值） */
    public static final Integer FEEDBACK_STATUS_UNHANDLED = 0;
    /** 处理状态：已回复 */
    public static final Integer FEEDBACK_STATUS_REPLIED = 1;
    /** 处理状态：已关闭 */
    public static final Integer FEEDBACK_STATUS_CLOSED = 2;

    // ====================== 反馈类型（对应表中feedback_type字段，TEXT类型，值与前端option的value一致） ======================
    /** 反馈类型：图书管需要添加图书，较重要！ */
    public static final String FEEDBACK_TYPE_ADD_BOOK = "add_book";
    /** 反馈类型：功能问题（前端value="function"） */
    public static final String FEEDBACK_TYPE_FUNCTION = "function";
    /** 反馈类型：体验优化（前端value="experience"） */
    public static final String FEEDBACK_TYPE_EXPERIENCE = "experience";
    /** 反馈类型：Bug反馈（前端value="bug"） */
    public static final String FEEDBACK_TYPE_BUG = "bug";
    /** 反馈类型：功能建议（前端value="suggestion"） */
    public static final String FEEDBACK_TYPE_SUGGESTION = "suggestion";
    /** 反馈类型：其他问题（前端value="other"） */
    public static final String FEEDBACK_TYPE_OTHER = "other";

    // 可选：拓展反馈类型的中文描述（用于后端返回给前端展示，不用存库）
    public static final String FEEDBACK_TYPE_ADD_BOOK_DESC = "请求添加书籍";
    public static final String FEEDBACK_TYPE_FUNCTION_DESC = "功能问题";
    public static final String FEEDBACK_TYPE_EXPERIENCE_DESC = "体验优化";
    public static final String FEEDBACK_TYPE_BUG_DESC = "Bug反馈";
    public static final String FEEDBACK_TYPE_SUGGESTION_DESC = "功能建议";
    public static final String FEEDBACK_TYPE_OTHER_DESC = "其他问题";

    // 私有构造器：禁止实例化常量类
    private FeedBackConstants() {}
}