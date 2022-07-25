package cn.karent.client2.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.karent.client2.util.CookieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/******************************************
 * 认证拦截器
 * @author wan
 * @date 2022.07.24 21:11
 ******************************************/
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthInterceptor.class);

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 验证是否登录, 没有登录的话跳转到SSO-Server中心
        HttpSession session = request.getSession();
        String token = (String) session.getAttribute("token");
        String tokenUrl = request.getParameter("token");
        String tokenHeader = request.getHeader("token");
        String tokenCookie = CookieUtils.get(request, "token2");
        LOGGER.info("当前访问地址:{}, sessionToken:{}, tokenUrl:{}, tokenHeader:{}, tokenCookie:{}",
                request.getRequestURL(),
                token, tokenUrl, tokenHeader, tokenCookie);
        if (token == null ) {
            String ret = "NO";
            token = null;
            if (!StrUtil.isEmpty(tokenUrl)) {
                token = tokenUrl;
            } else if (!StrUtil.isEmpty(tokenHeader)) {
                token = tokenHeader;
            } else if (!StrUtil.isEmpty(tokenCookie)) {
                token = tokenCookie;
            }
            if (token != null) {
                // 当用户未登录时, 去单点登录中心验证一下用户是否登录, 如果没有登录则跳转到登录页面
                ret = restTemplate.getForObject("http://localhost:8082/verify?token=" + token + "&logoutUrl=http://localhost:8081/logout", String.class);
            }
            if ("YES".equals(ret)) {
                // 创建局部会话
                session.setAttribute("token", token);
//                session.setMaxInactiveInterval(60);  // 设置session的过期时间
                // 将token设置到cookie当中, 这样下次来的时候就可以使用cookie中的token进行自动登录
                Cookie c = new Cookie("token2", token);
                c.setMaxAge(60);  // 1分钟
                response.addCookie(c);
            } else {
                // 获取当前的登录地址
                String url = request.getRequestURL().toString();
                // url编码
                response.sendRedirect("http://localhost:8082/loginUI?clientUrl=" + url);
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
