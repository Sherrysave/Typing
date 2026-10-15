package com.sjt.typing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sjt.typing.aop.RateLimit;
import com.sjt.typing.common.BusinessException;
import com.sjt.typing.common.R;
import com.sjt.typing.common.ResultCode;
import com.sjt.typing.dto.req.LoginReq;
import com.sjt.typing.dto.req.RefreshReq;
import com.sjt.typing.dto.req.SignupReq;
import com.sjt.typing.dto.req.UpdateProfileReq;
import com.sjt.typing.dto.vo.LoginVO;
import com.sjt.typing.dto.vo.UserProfileVO;
import com.sjt.typing.dto.vo.UserVO;
import com.sjt.typing.entity.Result;
import com.sjt.typing.entity.User;
import com.sjt.typing.security.RequireLogin;
import com.sjt.typing.security.UserContext;
import com.sjt.typing.service.ResultService;
import com.sjt.typing.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口（对标 api/routes/users）。
 */
@Tag(name = "用户", description = "注册 / 登录 / 刷新令牌 / 个人信息 / 个人主页")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ResultService resultService;

    public UserController(UserService userService, ResultService resultService) {
        this.userService = userService;
        this.resultService = resultService;
    }

    @Operation(summary = "注册")
    @RateLimit(key = "users.signup", limit = 5, windowSeconds = 60)
    @PostMapping("/signup")
    public R<LoginVO> signup(@Valid @RequestBody SignupReq req) {
        return R.ok(userService.signup(req));
    }

    @Operation(summary = "登录")
    @RateLimit(key = "users.login", limit = 5, windowSeconds = 60)
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginReq req) {
        return R.ok(userService.login(req));
    }

    @Operation(summary = "刷新令牌（同一 refreshToken 只能用一次）")
    @PostMapping("/refresh")
    public R<LoginVO> refresh(@Valid @RequestBody RefreshReq req) {
        return R.ok(userService.refresh(req));
    }

    @Operation(summary = "登出（吊销 refreshToken）")
    @RequireLogin
    @PostMapping("/logout")
    public R<Void> logout(@RequestBody(required = false) RefreshReq req) {
        if (req != null) {
            userService.logout(req.getRefreshToken());
        }
        return R.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @RequireLogin
    @GetMapping("/me")
    public R<UserVO> me() {
        String uid = UserContext.getUidOrThrow();
        User user = userService.getById(uid);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return R.ok(userService.toVO(user));
    }

    @Operation(summary = "修改个人资料")
    @RequireLogin
    @PatchMapping("/me")
    public R<Void> updateMe(@Valid @RequestBody UpdateProfileReq req) {
        userService.updateProfile(UserContext.getUidOrThrow(), req);
        return R.ok();
    }

    @Operation(summary = "他人主页（含最好成绩与统计）")
    @GetMapping("/{uid}/profile")
    public R<UserProfileVO> profile(@PathVariable String uid) {
        return R.ok(userService.profile(uid));
    }

    @Operation(summary = "分页查询指定用户的成绩")
    @RequireLogin
    @GetMapping("/{uid}/results")
    public R<Page<Result>> results(@PathVariable String uid,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return R.ok(resultService.listByUid(uid, page, size));
    }
}
