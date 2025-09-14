package com.ashwani.config;

import com.ashwani.enums.ConsistencyLevel;
import com.ashwani.enums.Region;
import com.ashwani.sharding.ConsistencyContext;
import com.ashwani.sharding.RegionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import static com.ashwani.constant.ApplicationConstant.CONSISTENCY_LEVEL_HEADER;
import static com.ashwani.constant.ApplicationConstant.REGION_HEADER;

@Component
public class RequestInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String regionHeader = request.getHeader(REGION_HEADER);
        if (regionHeader != null && !regionHeader.isEmpty()) {
            try {
                Region region = Region.valueOf(regionHeader.toUpperCase());
                RegionContext.setRegion(region);
            } catch (IllegalArgumentException e) {
                // Log or handle invalid region header
                System.err.println("Invalid X-Region header: " + regionHeader);
            }
        }

        String consistencyHeader = request.getHeader(CONSISTENCY_LEVEL_HEADER);
        if (consistencyHeader != null && !consistencyHeader.isEmpty()) {
            try {
                ConsistencyLevel consistencyLevel = ConsistencyLevel.valueOf(consistencyHeader.toUpperCase());
                ConsistencyContext.setConsistencyLevel(consistencyLevel);
            } catch (IllegalArgumentException e) {
                // Log or handle invalid consistency level header
                System.err.println("Invalid X-Consistency-Level header: " + consistencyHeader);
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Not used for this purpose
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RegionContext.clear();
        ConsistencyContext.clear();
    }
}
