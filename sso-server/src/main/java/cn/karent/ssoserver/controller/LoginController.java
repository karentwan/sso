package cn.karent.ssoserver.controller;

import cn.hutool.core.util.StrUtil;
import cn.karent.ssoserver.cache.LoginCache;
import cn.karent.ssoserver.util.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/******************************************
 * 登录
 * @author wan
 * @date 2022.07.20 21:18
 ******************************************/
@Controller
public class LoginController {

    private final static Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoginCache loginCache;

    @GetMapping("/loginUI")
    public String loginUI(Model model,
                          String clientUrl, String logoutUrl,
                          @CookieValue(value = "token", required = false) String token) {
        LOGGER.info("loginUI, clientUrl:{}， logoutUrl:{}, token:{}", clientUrl, logoutUrl, token);
        model.addAttribute("clientUrl", clientUrl);
        model.addAttribute("logoutUrl", logoutUrl);
        if (token != null) {  // 已登录, 将系统注册进系统里面
            loginCache.add(token, logoutUrl);
            // 重定向
            return "redirect:" + clientUrl + "?token=" + token;
        }
        return "login";
    }

    @PostMapping("/login")
    public void login(
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "clientUrl", required = false) String clientUrl,
            @RequestParam(value = "logoutUrl", required = false) String logoutUrl,
            @CookieValue(value = "token", required = false) String cookieToken,
            @RequestHeader(value = "token", required = false) String headerToken,
            HttpServletRequest req, HttpServletResponse resp) {
        // 当存在token的时候说明 已经有系统登录过
        if (!StrUtil.isEmpty(cookieToken)) {
            if (loginCache.hasKey(cookieToken)) {
                loginCache.add(cookieToken, logoutUrl);
                return;
            }
        }
        if (!StrUtil.isEmpty(headerToken)) {
            if (loginCache.hasKey(headerToken)) {
                loginCache.add(headerToken, logoutUrl);
                return;
            }
        }
        // 校验参数是否完整
        if (StrUtil.isEmpty(userName) && StrUtil.isEmpty(password)) {
            try {
                resp.getWriter().write("参数异常");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        LOGGER.info("用户名:{}, 密码:{}", userName, password);
        if ("admin".equals(userName) && "admin".equals(password)) {
            try {
                // 记录登录结果
                String token = TokenUtils.getToken();
                // 这里应该记录client的logout地址, 当client注销的时候就能取出来注销了
                if (!StrUtil.isEmpty(logoutUrl)) {
                    loginCache.add(token, logoutUrl);
                } else {
                    loginCache.add(token, "#");
                }
                // 写cookie
                Cookie cookie = new Cookie("token", token);
                cookie.setMaxAge(60);
                resp.addCookie(cookie);
                if (!StrUtil.isEmpty(clientUrl)) {
                    // 登录成功, 重定向回client1
                    resp.sendRedirect(clientUrl + "?token=" + token);
                } else {
                    resp.sendRedirect("success.html");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 登录失败, 返回失败数据
        try {
            resp.setCharacterEncoding("gbk");
            resp.getWriter().write("登录失败, 请重新登录...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/logout")
    public String logout(String token, HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("退出登录, token:{}", token);
        Set<String> urls = loginCache.get(token);
        Iterator<String> iter = urls.iterator();
        while (iter.hasNext()) {
            String url = iter.next();
            LOGGER.info("局部会话的注销地址为:{}", url);
            if (!"#".equals(url)) {
                String ret = null;
                try {
                    ret = restTemplate.getForObject(url + "?token=" + token, String.class);
                } catch (RestClientException e) {
                    e.printStackTrace();
                    LOGGER.error("局部会话注销异常, 错误信息为: {}", e.getLocalizedMessage());
                }
                LOGGER.info("局部会话销毁结果:{}", ret);
            }
            iter.remove();
        }
        // 删除cookie
        Cookie[] cookies = req.getCookies();
        for (Cookie cookie : cookies) {
            if ("token".equals(cookie.getName())) {
                // 删除cookie
                cookie.setMaxAge(0);
                resp.addCookie(cookie);
                break;
            }
        }
        return "redirect:/loginUI";
    }

    @GetMapping("/verify")
    @ResponseBody
    public String verify(String token) {
        if (loginCache.exists(token)) {
            return "YES";
        }
        return "NO";
    }

}
