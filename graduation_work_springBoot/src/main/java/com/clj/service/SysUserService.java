package com.clj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.clj.domain.SysUser;
import com.clj.domain.dto.SysUserManageDto;
import com.clj.domain.dto.SystemUserDto;

import java.util.Map;

/**
 * @author ajie
 * @description 针对表【sys_user(系统用户表)】的数据库操作Service
 * @createDate 2026-08-06 23:22:04
 */
public interface SysUserService extends IService<SysUser> {

    Page<SysUser> getUsersByPage(Integer pageNum, Integer pageSize);

    void addUser(SystemUserDto dto);

    /**
     * 管理端新增用户（不校验短信验证码），按 RBAC 写入用户-角色关联
     */
    void addManageUser(SysUserManageDto dto);

    void deleteUser(Long id);

    void updateUser(SysUser user);

    /**
     * 管理端编辑用户：基本信息 + 密码（可选）+ RBAC 角色重分配
     */
    void updateManageUser(SysUserManageDto dto);

    void updateCurrentUser(SysUser user);

    Page<SysUser> searchUsersByPage(String keyword, Integer pageNum, Integer pageSize);

    void updateUserStatus(Long id, Integer status);

    Map<String, String> searchUserByNameAndPhone(String name, String phone);

    /**
     * 根据姓名（昵称）和手机号查询拥有 USER 角色的承包人
     */
    SysUser getContractorByNameAndPhone(String name, String phone);

    Page<SysUser> getContractorsByPage(Integer pageNum, Integer pageSize);

    Page<SysUser> searchContractorsByPage(String keyword, Integer pageNum, Integer pageSize);

    SysUser getUserInfo();

    Map<String, String> getName();

    void updatePassword(String oldPassword, String newPassword);
}
