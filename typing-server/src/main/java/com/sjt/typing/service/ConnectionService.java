package com.sjt.typing.service;

import com.sjt.typing.dto.req.ConnectionReq;

/**
 * 关注关系业务（对标 dal/connections.ts）。
 */
public interface ConnectionService {

    /** 关注或取关（由 req.follow 决定） */
    void follow(ConnectionReq req, String uid);
}
