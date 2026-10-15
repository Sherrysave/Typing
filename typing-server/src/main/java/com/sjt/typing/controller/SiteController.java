package com.sjt.typing.controller;

import com.sjt.typing.common.R;
import com.sjt.typing.dto.vo.PsaVO;
import com.sjt.typing.dto.vo.PublicStatsVO;
import com.sjt.typing.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 站点公共接口（对标 api/routes/configs、psas、public），全部无需鉴权。
 */
@Tag(name = "站点公共", description = "全站配置 / 公告 / 公开统计")
@RestController
@RequestMapping("/api")
public class SiteController {

    private final ConfigService configService;

    public SiteController(ConfigService configService) {
        this.configService = configService;
    }

    @Operation(summary = "全站配置")
    @GetMapping("/configs")
    public R<Map<String, Object>> configs() {
        return R.ok(configService.getConfigs());
    }

    @Operation(summary = "站内公告")
    @GetMapping("/psas")
    public R<List<PsaVO>> psas() {
        return R.ok(configService.getActivePsas());
    }

    @Operation(summary = "公开统计")
    @GetMapping("/public/stats")
    public R<PublicStatsVO> publicStats() {
        return R.ok(configService.publicStats());
    }
}
