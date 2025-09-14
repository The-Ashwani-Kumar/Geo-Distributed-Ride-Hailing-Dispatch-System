package com.ashwani.sharding;

import com.ashwani.enums.Region;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class RegionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String regionHeader = request.getHeader("X-Region");
        if (regionHeader != null) {
            try {
                Region region = Region.valueOf(regionHeader.toUpperCase());
                RegionContextHolder.setRegion(region);
            } catch (IllegalArgumentException e) {
                // Handle invalid region string, maybe default to a region or throw an error
                // For now, we'll just ignore it
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RegionContextHolder.clear();
    }
}
