package com.sjt.typing.security;

/**
 * 当前登录用户的轻量视图，存放于 ThreadLocal 中供业务层读取。
 */
public class LoginUser {

    private String uid;
    private String name;
    private boolean admin;

    public LoginUser() {
    }

    public LoginUser(String uid, String name, boolean admin) {
        this.uid = uid;
        this.name = name;
        this.admin = admin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
