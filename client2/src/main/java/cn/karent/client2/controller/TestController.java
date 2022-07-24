package cn.karent.client2.controller;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/******************************************
 * 客户端2
 * @author wan
 * @date 2022.07.20 21:21
 ******************************************/
@Controller
public class TestController {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 三种方式获取token, 第一种是url重写, 第二种是header里面携带token, 第三种是cookie里面携带token
     * @param session
     * @param req
     * @param resp
     * @param tokenUrl
     * @param tokenHeader
     * @param tokenCookie
     * @return
     */
    @GetMapping("/test")
    public String test(HttpSession session, HttpServletRequest req, HttpServletResponse resp,
                        @RequestParam(value = "token", required = false) String tokenUrl,
                        @RequestHeader(value = "token", required = false) String tokenHeader,
                        @CookieValue(value = "token2", required = false) String tokenCookie) {
        System.out.println("client2 hello");
        String token = (String) session.getAttribute("token");
        if (token == null) {
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
                ret = restTemplate.getForObject("http://localhost:8082/verify?token=" + token, String.class);
            }
            if ("YES".equals(ret)) {
                // 创建局部会话
                session.setAttribute("token", token);
                // 将token设置到cookie当中, 这样下次来的时候就可以使用cookie中的token进行自动登录
                Cookie c = new Cookie("token2", token);
                c.setMaxAge(60);
                resp.addCookie(c);
            } else {
                // 获取当前的登录地址
                String url = req.getRequestURL().toString();
                System.out.println("当前登录地址: " + url);
                return "redirect:http://localhost:8082/loginUI?clientUrl=" + url + "&logoutUrl=http://localhost:8081/logout";
            }
        }
        return "hello";
    }



}
