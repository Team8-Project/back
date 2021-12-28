package com.teamproj.backend.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class StatisticsUtils {
    public static String getClientReferer() {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return req.getHeader("referer");
    }
    public static String getClientIp() {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = req.getHeader("X-Forwarded-For");

        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("x-real-ip");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("x-original-forwarded-for");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("HTTP_X_FORWARDED");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("HTTP_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("HTTP_FORWARDED");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("HTTP_VIA");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getHeader("REMOTE_ADDR");
        }
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
            ip = req.getRemoteAddr();
        }

        return ip;
    }
}
