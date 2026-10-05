package com.poti.common.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 允许向请求中添加/覆盖请求头的包装器。
 * <p>用于将 JWT 解析出的用户 ID 写入请求头，覆盖客户端伪造的 {@code X-User-Id}。</p>
 */
public class HeaderMapRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> headers = new HashMap<>();

    public HeaderMapRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        if (headers.containsKey(name)) {
            return headers.get(name);
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new HashSet<>(headers.keySet());
        Enumeration<String> e = super.getHeaderNames();
        while (e.hasMoreElements()) {
            names.add(e.nextElement());
        }
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (headers.containsKey(name)) {
            return Collections.enumeration(Collections.singleton(headers.get(name)));
        }
        return super.getHeaders(name);
    }
}