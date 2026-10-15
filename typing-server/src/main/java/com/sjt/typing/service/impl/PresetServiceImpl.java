package com.sjt.typing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.IdUtil;
import com.sjt.typing.common.JsonUtil;
import com.sjt.typing.common.ResultCode;
import com.sjt.typing.dto.req.PresetReq;
import com.sjt.typing.dto.vo.PresetVO;
import com.sjt.typing.entity.Preset;
import com.sjt.typing.mapper.PresetMapper;
import com.sjt.typing.service.PresetService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户预设业务实现。
 */
@Service
public class PresetServiceImpl implements PresetService {

    /** 单用户预设数量上限，防止把预设表当成无限存储 */
    private static final int MAX_PRESETS_PER_USER = 20;

    private final PresetMapper presetMapper;

    public PresetServiceImpl(PresetMapper presetMapper) {
        this.presetMapper = presetMapper;
    }

    @Override
    public List<PresetVO> list(String uid) {
        List<Preset> rows = presetMapper.selectList(new LambdaQueryWrapper<Preset>()
                .eq(Preset::getUid, uid)
                .orderByDesc(Preset::getUpdatedAt));
        return rows.stream().map(this::toVO).toList();
    }

    @Override
    public PresetVO create(PresetReq req, String uid) {
        Long count = presetMapper.selectCount(
                new LambdaQueryWrapper<Preset>().eq(Preset::getUid, uid));
        if (count != null && count >= MAX_PRESETS_PER_USER) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "预设数量已达上限（" + MAX_PRESETS_PER_USER + " 个）");
        }
        long now = System.currentTimeMillis();
        Preset entity = new Preset();
        entity.setId(IdUtil.randomId());
        entity.setUid(uid);
        entity.setName(req.getName());
        entity.setConfig(JsonUtil.toJson(req.getConfig()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        presetMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public void delete(String id, String uid) {
        Preset existing = presetMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "预设不存在");
        }
        if (!uid.equals(existing.getUid())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的预设");
        }
        presetMapper.deleteById(id);
    }

    private PresetVO toVO(Preset entity) {
        PresetVO vo = new PresetVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setConfig(JsonUtil.parse(entity.getConfig(), Object.class));
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
