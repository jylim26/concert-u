package com.concertu.ticketing.global.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class InstanceRequestLoggingFilter extends OncePerRequestFilter {

    private static final String HOST_NAME;
    private static final String HOST_ADDRESS;

    static {
        String hostName = "unknown-host";
        String hostAddress = "unknown-ip";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostName = localHost.getHostName();
            hostAddress = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            // fall back to defaults
        }
        HOST_NAME = hostName;
        HOST_ADDRESS = hostAddress;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            log.info(
                    "instance={}({}) method={} path={} status={} durationMs={}",
                    HOST_NAME,
                    HOST_ADDRESS,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs
            );
        }
    }
}
