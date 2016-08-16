package com.nibado.example.jwtangspr;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.web.util.WebUtils;

public class JwtFilter extends GenericFilterBean {


    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        final Cookie cookie = WebUtils.getCookie(request, "X-AUTH-TOKEN");
        if (cookie == null) {
            throw new ServletException("Missing or invalid Authorization header.");
        }

        try {
            final Claims claims = Jwts.parser().setSigningKey("secretkey")
                .parseClaimsJws(cookie.getValue()).getBody();
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
            StringRedisTemplate stringRedisTemplate = webApplicationContext.getBean(StringRedisTemplate.class);
            final String redisToken = stringRedisTemplate.opsForValue().get("Token:" + claims.getSubject());
            if (!cookie.getValue().equals(redisToken)) {
                throw new ServletException("Missing or invalid Authorization header.");
            }
            request.setAttribute("claims", claims);
        }
        catch (final SignatureException e) {
            throw new ServletException("Invalid token.");
        }

        chain.doFilter(req, res);
    }

}
