package com.clj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clj.common.exception.BusinessException;
import com.clj.domain.SysRole;
import com.clj.domain.SysUser;
import com.clj.domain.SysUserRole;
import com.clj.domain.dto.SysUserManageDto;
import com.clj.domain.dto.SystemUserDto;
import com.clj.mapper.SysRoleMapper;
import com.clj.mapper.SysUserMapper;
import com.clj.mapper.SysUserRoleMapper;
import com.clj.security.util.SecurityUtil;
import com.clj.service.SysUserService;
import com.clj.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author ajie
 * @description 针对表【sys_user(系统用户表)】的数据库操作Service实现
 * @createDate 2026-08-06 23:22:04
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SmsService smsService;

    /**
     * 查询拥有指定角色编码的用户ID子查询SQL片段
     */
    private static final String CONTRACTOR_ROLE_SUB_SQL =
            "SELECT sur.user_id FROM sys_user_role sur " +
            "INNER JOIN sys_role sr ON sur.role_id = sr.id " +
            "WHERE sr.role_code IN ('USER', 'ENTERPRISE_ADMIN') AND sr.deleted = 0";

    private static final String USER_ROLE_SUB_SQL =
            "SELECT sur.user_id FROM sys_user_role sur " +
            "INNER JOIN sys_role sr ON sur.role_id = sr.id " +
            "WHERE sr.role_code = 'USER' AND sr.deleted = 0";

    @Override
    public Page<SysUser> getUsersByPage(Integer pageNum, Integer pageSize) {
        Page<SysUser> page = this.lambdaQuery()
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        fillRoleCodes(page.getRecords());
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SystemUserDto dto) {
        // 1. 判断注册方式：手机号注册 or 邮箱注册
        boolean isPhoneRegister = dto.getPhone() != null && !dto.getPhone().isBlank();
        boolean isEmailRegister = dto.getEmail() != null && !dto.getEmail().isBlank();

        if (isPhoneRegister) {
            // 手机号注册：检查手机号是否已注册
            Long count = this.lambdaQuery()
                    .eq(SysUser::getPhone, dto.getPhone())
                    .eq(SysUser::getDeleted, 0)
                    .count();
            if (count > 0) {
                throw new BusinessException("手机号已注册");
            }
            // 检测手机验证码（Redis中）
            smsService.verifyCode(dto.getPhone(), dto.getCode(), "REGISTER");
        } else if (isEmailRegister) {
            // 邮箱注册：检查邮箱是否已注册
            Long count = this.lambdaQuery()
                    .eq(SysUser::getEmail, dto.getEmail())
                    .eq(SysUser::getDeleted, 0)
                    .count();
            if (count > 0) {
                throw new BusinessException("邮箱已注册");
            }
            // 检测邮箱验证码（Redis中）
            smsService.verifyCode(dto.getEmail(), dto.getCode(), "REGISTER");
        } else {
            throw new BusinessException("手机号和邮箱不能同时为空");
        }

        // 2. DTO 转 SysUser
        SysUser user = new SysUser();
        BeanUtil.copyProperties(dto, user);
        // 密码 BCrypt 加密存储
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setDeleted(0);
        user.setStatus(1);

        if (!this.save(user)) {
            throw new BusinessException("添加用户失败");
        }

        // 3. 根据 roleCode 查询角色，写入用户-角色关联表
        insertUserRole(user.getId(), dto.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addManageUser(SysUserManageDto dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BusinessException("密码不能为空");
        }
        // 检查用户名是否存在
        Long count = this.lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername())
                .eq(SysUser::getDeleted, 0)
                .count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            Long phoneCount = this.lambdaQuery()
                    .eq(SysUser::getPhone, dto.getPhone())
                    .eq(SysUser::getDeleted, 0)
                    .count();
            if (phoneCount > 0) {
                throw new BusinessException("手机号已注册");
            }
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        user.setDeleted(0);
        user.setCreateTime(new java.util.Date());
        if (!this.save(user)) {
            throw new BusinessException("添加用户失败");
        }

        // 按 RBAC 写入用户-角色关联
        insertUserRole(user.getId(), dto.getRoleCode());
    }

    /**
     * 根据角色编码查询角色并写入 sys_user_role（RBAC）
     */
    private void insertUserRole(Long userId, String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            throw new BusinessException("请选择用户角色");
        }
        SysRole role = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, roleCode)
                        .eq(SysRole::getDeleted, 0));
        if (role == null) {
            throw new BusinessException("角色不存在: " + roleCode);
        }

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        sysUserRoleMapper.insert(userRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 删除用户-角色关联（RBAC）
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        // 软删除用户
        if (!this.lambdaUpdate()
                .eq(SysUser::getId, id)
                .set(SysUser::getDeleted, 1)
                .update()) {
            throw new BusinessException("删除失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateManageUser(SysUserManageDto dto) {
        if (dto.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        SysUser user = this.lambdaQuery()
                .eq(SysUser::getId, dto.getId())
                .eq(SysUser::getDeleted, 0)
                .one();
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户名是否被其他用户使用
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            SysUser existingUser = this.lambdaQuery()
                    .eq(SysUser::getUsername, dto.getUsername())
                    .ne(SysUser::getId, dto.getId())
                    .eq(SysUser::getDeleted, 0)
                    .one();
            if (existingUser != null) {
                throw new BusinessException("用户名已存在");
            }
        }

        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        // 密码留空表示不修改
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setUpdateTime(new java.util.Date());
        user.setUpdateUid(SecurityUtil.getUserId());
        if (!this.updateById(user)) {
            throw new BusinessException("修改失败");
        }

        // 重新分配角色（先删后插，RBAC）
        if (dto.getRoleCode() != null && !dto.getRoleCode().isBlank()) {
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, dto.getId()));
            insertUserRole(dto.getId(), dto.getRoleCode());
        }
    }

    @Override
    public void updateUser(SysUser user) {
        // 检查用户名是否被其他用户使用
        if (user.getUsername() != null) {
            SysUser existingUser = this.lambdaQuery()
                    .eq(SysUser::getUsername, user.getUsername())
                    .ne(SysUser::getId, user.getId())
                    .eq(SysUser::getDeleted, 0)
                    .one();
            if (existingUser != null) {
                throw new BusinessException("用户名已存在");
            }
        }
        // 不允许修改密码（密码修改走单独接口）
        user.setPassword(null);
        if (!this.updateById(user)) {
            throw new BusinessException("修改失败");
        }
    }

    @Override
    public void updateCurrentUser(SysUser user) {
        Long currentUserId = SecurityUtil.getUserId();
        if (user == null || user.getId() == null || !currentUserId.equals(user.getId())) {
            throw new BusinessException("只能修改当前用户资料");
        }
        SysUser existing = this.lambdaQuery()
                .eq(SysUser::getId, currentUserId)
                .eq(SysUser::getDeleted, 0)
                .one();
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getUsername() != null && !user.getUsername().equals(existing.getUsername())) {
            boolean duplicate = this.lambdaQuery()
                    .eq(SysUser::getUsername, user.getUsername())
                    .ne(SysUser::getId, currentUserId)
                    .eq(SysUser::getDeleted, 0)
                    .exists();
            if (duplicate) {
                throw new BusinessException("用户名已存在");
            }
        }
        existing.setUsername(user.getUsername());
        existing.setNickname(user.getNickname());
        existing.setPhone(user.getPhone());
        existing.setEmail(user.getEmail());
        existing.setAvatar(user.getAvatar());
        existing.setUpdateTime(new java.util.Date());
        existing.setUpdateUid(currentUserId);
        if (!this.updateById(existing)) {
            throw new BusinessException("修改失败");
        }
    }

    @Override
    public Page<SysUser> searchUsersByPage(String keyword, Integer pageNum, Integer pageSize) {
        if (keyword == null || keyword.isBlank()) {
            return getUsersByPage(pageNum, pageSize);
        }
        Page<SysUser> page = this.lambdaQuery()
                .eq(SysUser::getDeleted, 0)
                .and(w -> w.like(SysUser::getNickname, keyword)
                        .or().like(SysUser::getUsername, keyword)
                        .or().likeRight(SysUser::getPhone, keyword))
                .orderByDesc(SysUser::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        fillRoleCodes(page.getRecords());
        return page;
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        if (!this.lambdaUpdate()
                .eq(SysUser::getId, id)
                .set(SysUser::getStatus, status)
                .update()) {
            throw new BusinessException("修改失败");
        }
    }

    @Override
    public Map<String, String> searchUserByNameAndPhone(String name, String phone) {
        SysUser user = getContractorByNameAndPhone(name, phone);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Map<String, String> map = new HashMap<>();
        map.put("userId", user.getId().toString());
        return map;
    }

    @Override
    public SysUser getContractorByNameAndPhone(String name, String phone) {
        // 按 RBAC：仅查询拥有 USER 角色的承包人
        return this.lambdaQuery()
                .eq(SysUser::getNickname, name)
                .eq(SysUser::getPhone, phone)
                .eq(SysUser::getDeleted, 0)
                .apply("id IN (" + USER_ROLE_SUB_SQL + ")")
                .one();
    }

    @Override
    public Page<SysUser> getContractorsByPage(Integer pageNum, Integer pageSize) {
        Page<SysUser> page = this.lambdaQuery()
                .eq(SysUser::getDeleted, 0)
                .apply("id IN (" + CONTRACTOR_ROLE_SUB_SQL + ")")
                .orderByDesc(SysUser::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        fillRoleCodes(page.getRecords());
        return page;
    }

    @Override
    public Page<SysUser> searchContractorsByPage(String keyword, Integer pageNum, Integer pageSize) {
        if (keyword == null || keyword.isBlank()) {
            return getContractorsByPage(pageNum, pageSize);
        }
        Page<SysUser> page = this.lambdaQuery()
                .eq(SysUser::getDeleted, 0)
                .apply("id IN (" + CONTRACTOR_ROLE_SUB_SQL + ")")
                .and(w -> w.like(SysUser::getNickname, keyword)
                        .or().likeRight(SysUser::getPhone, keyword))
                .orderByDesc(SysUser::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
        fillRoleCodes(page.getRecords());
        return page;
    }

    /**
     * 批量回填用户的 RBAC 角色编码（取第一个角色），供列表展示
     */
    private void fillRoleCodes(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        List<Long> userIds = users.stream()
                .map(SysUser::getId)
                .collect(Collectors.toList());

        // 1. 查询用户-角色关联
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getUserId, userIds));
        if (userRoles.isEmpty()) {
            return;
        }

        // 2. 查询角色信息
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, SysRole> roleMap = sysRoleMapper.selectList(
                        new LambdaQueryWrapper<SysRole>()
                                .in(SysRole::getId, roleIds)
                                .eq(SysRole::getDeleted, 0))
                .stream()
                .collect(Collectors.toMap(SysRole::getId, Function.identity(), (k1, k2) -> k1));

        // 3. userId -> roleCode
        Map<Long, String> userRoleCodeMap = userRoles.stream()
                .filter(ur -> roleMap.containsKey(ur.getRoleId()))
                .collect(Collectors.toMap(SysUserRole::getUserId,
                        ur -> roleMap.get(ur.getRoleId()).getRoleCode(), (k1, k2) -> k1));

        users.forEach(user -> {
            user.setRoleCode(userRoleCodeMap.get(user.getId()));
            // 脱敏：不返回密码哈希
            user.setPassword(null);
        });
    }

    @Override
    public SysUser getUserInfo() {
        SysUser user = this.lambdaQuery()
                .eq(SysUser::getId, SecurityUtil.getUserId())
                .eq(SysUser::getDeleted, 0)
                .one();
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 脱敏：不返回密码
        user.setPassword(null);
        fillRoleCodes(List.of(user));
        return user;
    }

    @Override
    public Map<String, String> getName() {
        SysUser user = this.lambdaQuery()
                .eq(SysUser::getId, SecurityUtil.getUserId())
                .eq(SysUser::getDeleted, 0)
                .one();
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Map<String, String> map = new HashMap<>();
        map.put("name", user.getNickname());
        return map;
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        SysUser user = this.lambdaQuery()
                .eq(SysUser::getId, SecurityUtil.getUserId())
                .eq(SysUser::getDeleted, 0)
                .one();
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 兼容 BCrypt 与存量明文密码
        String dbPassword = user.getPassword();
        boolean oldMatched = dbPassword.startsWith("$2")
                ? passwordEncoder.matches(oldPassword, dbPassword)
                : dbPassword.equals(oldPassword);
        if (!oldMatched) {
            throw new BusinessException("旧密码错误");
        }

        // 新密码 BCrypt 加密存储
        if (!this.lambdaUpdate()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getPassword, passwordEncoder.encode(newPassword))
                .update()) {
            throw new BusinessException("密码修改失败");
        }
    }
}
