package com.ashwani.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import static com.ashwani.constant.ApplicationConstant.CONSISTENCY_HEADER;
import static com.ashwani.constant.ApplicationConstant.DEFAULT_CONSISTENCY;

@Component
public class ConsistencyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String consistencyLevel = request.getHeader(CONSISTENCY_HEADER);
        if (consistencyLevel == null || consistencyLevel.isEmpty()) {
            consistencyLevel = DEFAULT_CONSISTENCY;
        }
        ConsistencyContext.setConsistencyLevel(consistencyLevel);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Not used for this interceptor
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ConsistencyContext.clearConsistencyLevel();
    }
}
