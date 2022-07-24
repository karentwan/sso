package cn.karent.client1.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/******************************************
 * cookie操作的工具类
 * @author wan
 * @date 2022.07.24 21:19
 ******************************************/
public class CookieUtils {

    public static String get(HttpServletRequest req, String key) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        Object[] objs = Arrays.stream(cookies)
                .filter(k -> k.getName().equals(key))
                .map(Cookie::getValue)
                .toArray();
        return objs.length == 0 ? null : (String) objs[0];
    }

    public static void addCookie(HttpServletResponse resp, String key, String value) {
        Cookie c = new Cookie(key, value);
        resp.addCookie(c);
    }

}
