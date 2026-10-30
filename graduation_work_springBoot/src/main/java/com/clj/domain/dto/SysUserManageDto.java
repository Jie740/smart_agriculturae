package com.clj.domain.dto;

import lombok.Data;

/**
 * 管理端（系统管理员/企业管理员）新增、编辑用户 DTO
 * 用户数据存 sys_user 表，角色按 RBAC 存 sys_user_role 表
 */
@Data
public class SysUserManageDto {
    /**
     * 用户ID（编辑时使用，新增时为空）
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（新增必填；编辑留空表示不修改）
     */
    private String password;

    /**
     * 昵称（姓名）
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 1正常 0禁用
     */
    private Integer status;

    /**
     * 角色编码（SYSTEM_ADMIN、ENTERPRISE_ADMIN、USER）
     */
    private String roleCode;
}
