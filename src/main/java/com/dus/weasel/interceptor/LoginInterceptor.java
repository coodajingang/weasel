package com.dus.weasel.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.dus.weasel.domain.UserInfo;

public class LoginInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 验证用户是否登陆
        Object obj = request.getSession().getAttribute("user");
        if (obj == null || !(obj instanceof UserInfo)) {
            String url = "/login";
            String requestURI = request.getRequestURI();
            url = url + "?redirect=" + requestURI;
            response.sendRedirect(url);
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    
}
