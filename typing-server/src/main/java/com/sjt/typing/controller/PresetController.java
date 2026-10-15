package com.sjt.typing.controller;

import com.sjt.typing.common.R;
import com.sjt.typing.dto.req.PresetReq;
import com.sjt.typing.dto.vo.PresetVO;
import com.sjt.typing.security.RequireLogin;
import com.sjt.typing.security.UserContext;
import com.sjt.typing.service.PresetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户预设接口（对标 api/routes/presets）。
 */
@Tag(name = "预设", description = "用户配置预设读写")
@RestController
@RequestMapping("/api/presets")
public class PresetController {

    private final PresetService presetService;

    public PresetController(PresetService presetService) {
        this.presetService = presetService;
    }

    @Operation(summary = "查询我的预设")
    @RequireLogin
    @GetMapping
    public R<List<PresetVO>> list() {
        return R.ok(presetService.list(UserContext.getUidOrThrow()));
    }

    @Operation(summary = "新建预设")
    @RequireLogin
    @PostMapping
    public R<PresetVO> create(@Valid @RequestBody PresetReq req) {
        return R.ok(presetService.create(req, UserContext.getUidOrThrow()));
    }

    @Operation(summary = "删除预设")
    @RequireLogin
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        presetService.delete(id, UserContext.getUidOrThrow());
        return R.ok();
    }
}
