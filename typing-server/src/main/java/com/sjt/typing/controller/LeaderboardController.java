package com.sjt.typing.controller;

import com.sjt.typing.common.R;
import com.sjt.typing.dto.vo.LeaderboardVO;
import com.sjt.typing.security.RequireLogin;
import com.sjt.typing.security.UserContext;
import com.sjt.typing.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 排行榜接口（对标 api/routes/leaderboards）。
 */
@Tag(name = "排行榜", description = "榜单查询 / 我的名次")
@RestController
@RequestMapping("/api/leaderboards")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Operation(summary = "查询排行榜")
    @GetMapping
    public R<List<LeaderboardVO>> top(@RequestParam(defaultValue = "time") String mode,
                                      @RequestParam(defaultValue = "15") String mode2,
                                      @RequestParam(defaultValue = "english") String language,
                                      @RequestParam(defaultValue = "50") int limit) {
        return R.ok(leaderboardService.getTop(mode, mode2, language, limit));
    }

    @Operation(summary = "查询我的名次")
    @RequireLogin
    @GetMapping("/rank")
    public R<Integer> myRank(@RequestParam(defaultValue = "time") String mode,
                             @RequestParam(defaultValue = "15") String mode2,
                             @RequestParam(defaultValue = "english") String language) {
        String uid = UserContext.getUidOrThrow();
        return R.ok(leaderboardService.getRank(uid, mode, mode2, language));
    }
}
