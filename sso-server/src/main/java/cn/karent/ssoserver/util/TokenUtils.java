package cn.karent.ssoserver.util;

import java.util.UUID;

/******************************************
 * 生成token的工具类
 * @author wan
 * @date 2022.07.21 21:36
 ******************************************/
public class TokenUtils {

    public static String getToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
