package com.clj.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clj.common.exception.BusinessException;
import com.clj.domain.*;
import com.clj.domain.dto.EquipmentApplyDto;
import com.clj.domain.vo.EquipmentApplyVo;
import com.clj.domain.vo.EquipmentDetailVo;
import com.clj.mapper.EquipmentApplyMapper;
import com.clj.security.util.SecurityUtil;
import com.clj.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author ajie
* @description 针对表【equipment_apply(设备申请表)】的数据库操作Service实现
* @createDate 2026-03-02 20:08:34
*/
@Service
@RequiredArgsConstructor
public class EquipmentApplyServiceImpl extends ServiceImpl<EquipmentApplyMapper, EquipmentApply>
    implements EquipmentApplyService{
    final SysUserService sysUserService;
    final EquipmentService equipmentService;
    final EquipmentRecordService equipmentRecordService;
    final EquipmentTypeService equipmentTypeService;

    @Override
    public void add(EquipmentApplyDto equipmentApplyDto) {
        SysUser user = sysUserService.lambdaQuery().eq(SysUser::getPhone, equipmentApplyDto.getPhone())
                .eq(SysUser::getNickname, equipmentApplyDto.getApplicant())
                .eq(SysUser::getDeleted, 0)
                .one();
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        EquipmentApply equipmentApply = new EquipmentApply();
        equipmentApply.setApplicantId(user.getId());
        BeanUtils.copyProperties(equipmentApplyDto, equipmentApply);
        if (!save(equipmentApply)) {
            throw new BusinessException("添加失败");
        }
    }

    @Override
    public void delete(Integer applyId) {
        EquipmentApply apply = getById(applyId);
        if (apply == null) {
            throw new BusinessException("申请不存在");
        }
        if (!isAdministrator() && !SecurityUtil.getUserId().equals(apply.getApplicantId())) {
            throw new BusinessException("无权操作该申请");
        }
        if (!removeById(applyId)) {
            throw new BusinessException("删除失败");
        }
    }

    private boolean isAdministrator() {
        return SecurityUtil.getLoginUser().getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(authority -> "ROLE_SYSTEM_ADMIN".equals(authority)
                        || "ROLE_ENTERPRISE_ADMIN".equals(authority));
    }

    @Override
    public void updateApply(EquipmentApplyDto equipmentApplyDto) {
        SysUser user = sysUserService.lambdaQuery().eq(SysUser::getPhone, equipmentApplyDto.getPhone())
                .eq(SysUser::getNickname, equipmentApplyDto.getApplicant())
                .eq(SysUser::getDeleted, 0)
                .one();
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        EquipmentApply equipmentApply = new EquipmentApply();
        equipmentApply.setApplicantId(user.getId());
        BeanUtils.copyProperties(equipmentApplyDto, equipmentApply);
        if (!updateById(equipmentApply)) {
            throw new BusinessException("更新失败");
        }
    }

    @Override
    public Page<EquipmentApplyVo> getApplyByPage(Integer pageNum, Integer pageSize) {
        Page<EquipmentApply> page = this.lambdaQuery().page(new Page<>(pageNum, pageSize));

        // 批量查询所有申请人 ID 和设备 ID
        List<EquipmentApply> applies = page.getRecords();
        Set<Long> applicantIds = applies.stream()
                .map(EquipmentApply::getApplicantId)
                .collect(Collectors.toSet());
        Set<Long> equipmentIds = applies.stream()
                .map(EquipmentApply::getEquipmentId)
                .collect(Collectors.toSet());

        // 批量查询用户列表和设备列表
        List<SysUser> users = sysUserService.lambdaQuery()
                .eq(SysUser::getDeleted, 0)
                .in(SysUser::getId, applicantIds)
                .list();
        List<Equipment> equipments = equipmentService.lambdaQuery()
                .in(Equipment::getEquipmentId, equipmentIds)
                .list();

        // 构建映射关系
        java.util.Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));
        java.util.Map<Long, Equipment> equipmentMap = equipments.stream()
                .collect(Collectors.toMap(Equipment::getEquipmentId, equipment -> equipment));

        // 组装 VO 对象
        ArrayList<EquipmentApplyVo> equipmentApplyVos = new ArrayList<>();
        for (EquipmentApply apply : applies) {
            EquipmentApplyVo equipmentApplyVo = new EquipmentApplyVo();
            BeanUtils.copyProperties(apply, equipmentApplyVo);

            SysUser user = userMap.get(apply.getApplicantId());
            if (user != null) {
                equipmentApplyVo.setApplicant(user.getNickname());
                equipmentApplyVo.setPhone(user.getPhone());
            }

            Equipment equipment = equipmentMap.get(apply.getEquipmentId());
            if (equipment != null) {
                equipmentApplyVo.setEquipmentName(equipment.getEquipmentName());
            }

            equipmentApplyVos.add(equipmentApplyVo);
        }

        Page<EquipmentApplyVo> equipmentApplyVoPage = new Page<>(pageNum, pageSize, page.getTotal());
        equipmentApplyVoPage.setRecords(equipmentApplyVos);
        return equipmentApplyVoPage;
    }

    @Override
    public Page<EquipmentApplyVo> searchApplyByPage(String keyword, Integer pageNum, Integer pageSize) {
        if (keyword == null){
            return getApplyByPage(pageNum, pageSize);
        }

        // 根据 keyword 查询用户 ID 列表（模糊匹配姓名或手机号）
        List<SysUser> users = sysUserService.lambdaQuery()
                .eq(SysUser::getDeleted, 0)
                .like(SysUser::getNickname, keyword)
                .list();
        Set<Long> userIds = users.stream()
                .map(SysUser::getId)
                .collect(Collectors.toSet());

        // 根据 keyword 查询设备 ID 列表（模糊匹配设备名称）
        List<Equipment> equipments = equipmentService.lambdaQuery()
                .like(Equipment::getEquipmentName, keyword)
                .list();
        Set<Long> equipmentIds = equipments.stream()
                .map(Equipment::getEquipmentId)
                .collect(Collectors.toSet());

        // 根据用户 ID 和设备 ID 分页查询申请记录
        Page<EquipmentApply> page = new Page<>(pageNum, pageSize);
        Page<EquipmentApply> applyPage = this.lambdaQuery()
                .in(!userIds.isEmpty(), EquipmentApply::getApplicantId, userIds)
                .or()
                .in(!equipmentIds.isEmpty(), EquipmentApply::getEquipmentId, equipmentIds)
                .page(page);

        // 批量查询申请人和设备信息
        List<EquipmentApply> applies = applyPage.getRecords();
        Set<Long> applicantIds = applies.stream()
                .map(EquipmentApply::getApplicantId)
                .collect(Collectors.toSet());
        Set<Long> applyEquipmentIds = applies.stream()
                .map(EquipmentApply::getEquipmentId)
                .collect(Collectors.toSet());

        // 批量查询用户列表和设备列表
        List<SysUser> userList = sysUserService.lambdaQuery()
                .eq(SysUser::getDeleted, 0)
                .in(SysUser::getId, applicantIds)
                .list();
        List<Equipment> equipmentList = equipmentService.lambdaQuery()
                .in(Equipment::getEquipmentId, applyEquipmentIds)
                .list();

        // 构建映射关系
        java.util.Map<Long, SysUser> userMap = userList.stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));
        java.util.Map<Long, Equipment> equipmentMap = equipmentList.stream()
                .collect(Collectors.toMap(Equipment::getEquipmentId, equipment -> equipment));

        // 组装 VO 对象
        ArrayList<EquipmentApplyVo> equipmentApplyVos = new ArrayList<>();
        for (EquipmentApply apply : applies) {
            EquipmentApplyVo equipmentApplyVo = new EquipmentApplyVo();
            BeanUtils.copyProperties(apply, equipmentApplyVo);

            SysUser user = userMap.get(apply.getApplicantId());
            if (user != null) {
                equipmentApplyVo.setApplicant(user.getNickname());
                equipmentApplyVo.setPhone(user.getPhone());
            }

            Equipment equipment = equipmentMap.get(apply.getEquipmentId());
            if (equipment != null) {
                equipmentApplyVo.setEquipmentName(equipment.getEquipmentName());
            }

            equipmentApplyVos.add(equipmentApplyVo);
        }

        Page<EquipmentApplyVo> equipmentApplyVoPage = new Page<>(pageNum, pageSize, applyPage.getTotal());
        equipmentApplyVoPage.setRecords(equipmentApplyVos);
        return equipmentApplyVoPage;
    }

    @Override
    @Transactional
    public void updateApplyStatus(Long applyId, Integer status) {
        EquipmentApply apply = this.lambdaQuery().eq(EquipmentApply::getApplyId, applyId).one();
        Long equipmentId = apply.getEquipmentId();
        //查询设备是否存在
        Equipment equipment = equipmentService.lambdaQuery().eq(Equipment::getEquipmentId, equipmentId).one();
        if (equipment == null) {
            throw new BusinessException("设备不存在");
        }
        //审批通过
        if (status == 1) {
            Integer equipmentStatus = equipment.getStatus();
            if (equipmentStatus == 1) {
                throw new BusinessException("设备已借出");
            }
            if (equipmentStatus == 2) {
                throw new BusinessException("设备正在报修");
            }
            if (equipmentStatus == 3) {
                throw new BusinessException("设备已损坏");
            }
            //更新设备状态
            equipmentService.lambdaUpdate().eq(Equipment::getEquipmentId, equipmentId)
                    .set(Equipment::getStatus, 1)
                    .update();
            //增加用户设备记录
            EquipmentRecord equipmentRecord = new EquipmentRecord();
            equipmentRecord.setEquipmentId(equipmentId);
            equipmentRecord.setOwnerId(apply.getApplicantId());
            equipmentRecordService.add(equipmentRecord);
            //修改申请状态
            this.lambdaUpdate().eq(EquipmentApply::getApplyId, applyId)
                    .set(EquipmentApply::getStatus, status)
                    .update();
            return;
        }
        //审批未通过
        if (status == 2) {
            this.lambdaUpdate().eq(EquipmentApply::getApplyId, applyId)
                    .set(EquipmentApply::getStatus, status)
                    .update();
            return;
        }
        throw new BusinessException("参数错误");
    }

    @Override
    public EquipmentApplyVo getApplyVoById(Long applyId) {
        EquipmentApply apply = this.lambdaQuery().eq(EquipmentApply::getApplyId, applyId)
                .one();
        EquipmentApplyVo equipmentApplyVo = new EquipmentApplyVo();
        BeanUtils.copyProperties(apply, equipmentApplyVo);
        SysUser one = sysUserService.lambdaQuery()
                .eq(SysUser::getId, apply.getApplicantId())
                .eq(SysUser::getDeleted, 0)
                .one();
        equipmentApplyVo.setApplicant(one.getNickname());
        equipmentApplyVo.setPhone(one.getPhone());
        Equipment equipment = equipmentService.lambdaQuery().eq(Equipment::getEquipmentId, apply.getEquipmentId()).one();
        equipmentApplyVo.setEquipmentName(equipment.getEquipmentName());
        return equipmentApplyVo;
    }

    @Override
    public EquipmentDetailVo getEquipmentNameAndTypeNameById(Long applyId) {
        EquipmentDetailVo equipmentDetailVo = new EquipmentDetailVo();
        EquipmentApply one = this.lambdaQuery().eq(EquipmentApply::getApplyId, applyId).one();
        Long equipmentId = one.getEquipmentId();
        Equipment one1 = equipmentService.lambdaQuery().eq(Equipment::getEquipmentId, equipmentId).one();
        Long equipmentTypeId = one1.getEquipmentTypeId();
        String typeName = null;
        if (equipmentTypeId != null) {
            EquipmentType one2 = equipmentTypeService.lambdaQuery().eq(EquipmentType::getEquipmentTypeId, equipmentTypeId).one();
            if (one2 != null) {
                typeName = one2.getEquipmentTypeName();
            }
        }
        equipmentDetailVo.setEquipmentId(equipmentId);
        equipmentDetailVo.setEquipmentName(one1.getEquipmentName());
        equipmentDetailVo.setTypeName(typeName);
        return equipmentDetailVo;
    }

    @Override
    public Page<EquipmentApplyVo> getMyApplies(String keyword, Integer pageNum, Integer pageSize) {
        // 1. 从 SecurityContext 获取当前用户ID
        Long userId = SecurityUtil.getUserId();

        // 2. 构建查询条件
        var queryWrapper = this.lambdaQuery()
                .eq(EquipmentApply::getApplicantId, userId)
                .orderByDesc(EquipmentApply::getApplyTime);

        // 3. 如果有关键词，根据设备名模糊查询
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 先查询匹配的设备ID
            List<Equipment> matchedEquipments = equipmentService.lambdaQuery()
                    .like(Equipment::getEquipmentName, keyword)
                    .list();

            if (matchedEquipments.isEmpty()) {
                Page<EquipmentApplyVo> emptyPage = new Page<>(pageNum, pageSize, 0);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }

            Set<Long> equipmentIds = matchedEquipments.stream()
                    .map(Equipment::getEquipmentId)
                    .collect(Collectors.toSet());

            queryWrapper.in(EquipmentApply::getEquipmentId, equipmentIds);
        }

        // 4. 分页查询
        Page<EquipmentApply> page = queryWrapper.page(new Page<>(pageNum, pageSize));

        // 5. 批量查询申请人和设备信息
        List<EquipmentApply> applies = page.getRecords();
        Set<Long> applicantIds = applies.stream()
                .map(EquipmentApply::getApplicantId)
                .collect(Collectors.toSet());
        Set<Long> applyEquipmentIds = applies.stream()
                .map(EquipmentApply::getEquipmentId)
                .collect(Collectors.toSet());

        // 6. 批量查询用户列表和设备列表
        List<SysUser> userList = sysUserService.lambdaQuery()
                .eq(SysUser::getDeleted, 0)
                .in(SysUser::getId, applicantIds)
                .list();
        List<Equipment> equipmentList = equipmentService.lambdaQuery()
                .in(Equipment::getEquipmentId, applyEquipmentIds)
                .list();

        // 7. 构建映射关系
        java.util.Map<Long, SysUser> userMap = userList.stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));
        java.util.Map<Long, Equipment> equipmentMap = equipmentList.stream()
                .collect(Collectors.toMap(Equipment::getEquipmentId, equipment -> equipment));

        // 8. 组装 VO 对象
        ArrayList<EquipmentApplyVo> equipmentApplyVos = new ArrayList<>();
        for (EquipmentApply apply : applies) {
            EquipmentApplyVo equipmentApplyVo = new EquipmentApplyVo();
            BeanUtils.copyProperties(apply, equipmentApplyVo);

            SysUser user = userMap.get(apply.getApplicantId());
            if (user != null) {
                equipmentApplyVo.setApplicant(user.getNickname());
                equipmentApplyVo.setPhone(user.getPhone());
            }

            Equipment equipment = equipmentMap.get(apply.getEquipmentId());
            if (equipment != null) {
                equipmentApplyVo.setEquipmentName(equipment.getEquipmentName());
            }

            equipmentApplyVos.add(equipmentApplyVo);
        }

        Page<EquipmentApplyVo> equipmentApplyVoPage = new Page<>(pageNum, pageSize, page.getTotal());
        equipmentApplyVoPage.setRecords(equipmentApplyVos);
        return equipmentApplyVoPage;
    }
}


