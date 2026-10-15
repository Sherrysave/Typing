package com.sjt.typing.config;

import com.sjt.typing.mapper.AdminUidMapper;
import com.sjt.typing.security.AdminAuthInterceptor;
import com.sjt.typing.security.AuthInterceptor;
import com.sjt.typing.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：跨域、鉴权拦截器注册，以及前端静态资源暴露。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;
    private final AdminUidMapper adminUidMapper;

    /**
     * 前端静态文件目录：默认指向 typing-server 的上级目录（即 Typing 仓库根，存放 index.html）。
     * 让前端与后端同源，省去跨域，同时使「?r={code}」分享短链有真实可打开的落地页。
     */
    @Value("${typing.frontend.dir:file:../}")
    private String frontendDir;

    public WebMvcConfig(JwtUtil jwtUtil, AdminUidMapper adminUidMapper) {
        this.jwtUtil = jwtUtil;
        this.adminUidMapper = adminUidMapper;
    }

    /**
     * 注册鉴权拦截器，拦截所有 /api/** 请求。
     * <p>注册顺序即执行顺序：AdminAuthInterceptor 在 AuthInterceptor 之后，
     * 因此它可以直接信任 UserContext 里已解析好的登录态。</p>
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtUtil))
                .addPathPatterns("/api/**");
        registry.addInterceptor(new AdminAuthInterceptor(adminUidMapper))
                .addPathPatterns("/api/admin/**");
    }

    /**
     * CORS：本地前端可能以 file:// 打开（Origin 为 null），故用 allowedOriginPatterns
     * 放行全部来源并开启凭证；暴露 Authorization 头供前端读取。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 静态资源：仅暴露前端页面与教学配图，避免把仓库其他文件也一并对外。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/index.html", "/finger-chart.png")
                .addResourceLocations(frontendDir);
    }

    /**
     * 根路径 → index.html，使分享短链 {@code /?r={code}} 能直接打开前端页面。
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
