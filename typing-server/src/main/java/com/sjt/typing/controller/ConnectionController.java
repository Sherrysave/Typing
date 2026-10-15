package com.sjt.typing.controller;

import com.sjt.typing.common.R;
import com.sjt.typing.dto.req.ConnectionReq;
import com.sjt.typing.security.RequireLogin;
import com.sjt.typing.security.UserContext;
import com.sjt.typing.service.ConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关注关系接口（对标 api/routes/connections）。
 */
@Tag(name = "关注", description = "关注 / 取关")
@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @Operation(summary = "关注或取关")
    @RequireLogin
    @PostMapping
    public R<Void> follow(@Valid @RequestBody ConnectionReq req) {
        connectionService.follow(req, UserContext.getUidOrThrow());
        return R.ok();
    }
}
