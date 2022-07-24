package cn.karent.client1.controller;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/******************************************
 *
 * @author wan
 * @date 2022.07.24 21:50
 ******************************************/
@Controller
public class LoginController {

    @GetMapping("/logout")
    @ResponseBody
    public void logout(@RequestParam(value = "token", required = false) String token,
                       @SessionAttribute(value = "token", required = false) String sessionToken,
                       @CookieValue(value = "token1", required = false) String cookieToken,
                       HttpSession session, HttpServletResponse resp) {
        System.out.println("客户端1退出登录, token为:" + token + "\tsessionToken:" + sessionToken);
        if (!StrUtil.isEmpty(token)) {  // 来自SSO中心
            // 清楚session
            session.setAttribute("token", null);
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
            try {
                resp.sendRedirect("http://localhost:8082/logout?token=" + cookieToken);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
