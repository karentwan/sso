package cn.karent.client2.controller;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/******************************************
 * 登录
 * @author wan
 * @date 2022.07.24 22:01
 ******************************************/
@Controller
public class LoginController {

    private final static Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @GetMapping("/logout")
    public void logout(@RequestParam(value = "token", required = false) String token,
                       @SessionAttribute(value = "token", required = false) String sessionToken,
                       @CookieValue(value = "token2", required = false) String cookieToken,
                       HttpSession session, HttpServletResponse resp) {
        LOGGER.info("客户端2退出登录, tokenUrl:{}， tokenSession:{}, tokenCookie:{}", token, sessionToken, cookieToken);
        // 清除session
        session.setAttribute("token", null);
        if (!StrUtil.isEmpty(token)) {  // 来自SSO中心
            try {
                resp.getWriter().write("YES");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!StrUtil.isEmpty(sessionToken)) {  // 来自客户端
            // 单点中心退出
            try {
                resp.sendRedirect("http://localhost:8082/logout?token=" + sessionToken);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!StrUtil.isEmpty(cookieToken)) {
            // 单点中心退出
            try {
                resp.sendRedirect("http://localhost:8082/logout?token=" + cookieToken);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
