package com.muxin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    private static final String[] IGNORED_PATHS = {
        "/api/health",
        "/api/messages"
    };

    private boolean shouldLog(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String path : IGNORED_PATHS) {
            if (uri.equals(path)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!shouldLog(request)) {
            return true;
        }

        logger.info("========== 请求开始 ==========");
        logger.info("请求方法: {}", request.getMethod());
        logger.info("请求URL: {}", request.getRequestURL());
        logger.info("请求URI: {}", request.getRequestURI());

        String contentType = request.getContentType();
        if (contentType != null) {
            logger.info("Content-Type: {}", contentType);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (!shouldLog(request)) {
            return;
        }

        logger.info("========== 请求完成 ==========");
        logger.info("响应状态码: {}", response.getStatus());
        if (ex != null) {
            logger.error("异常信息: ", ex);
        }
        logger.info("================================\n");
    }
}
