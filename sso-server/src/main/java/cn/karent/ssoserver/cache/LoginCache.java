package cn.karent.ssoserver.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/******************************************
 * 登录缓存, 使用本地内存(可使用Redis代替)
 * @author wan
 * @date 2022.07.20 21:50
 ******************************************/
@Component
public class LoginCache {

    private final static Logger LOGGER = LoggerFactory.getLogger(LoginCache.class);

    @Autowired
    private RestTemplate restTemplate;

    private final Map<String, Set<String>> map = new ConcurrentHashMap<>();

    public void add(String token, String clientUrl) {
        if (!map.containsKey(token)) {
            synchronized (map) {
                if (!map.containsKey(token)) {
                    map.put(token, new HashSet<>());
                }
            }
        }
        Set<String> urls = map.get(token);
        synchronized (urls) {
            urls.add(clientUrl);
        }
    }

    public void init(String token) {
        map.put(token, new HashSet<>());
    }

    public void remove(String token) {
        if (map.containsKey(token)) {
            synchronized (map) {
                if (map.containsKey(token)) {
                    Set<String> urls = map.get(token);
                    Iterator<String> iter = urls.iterator();
                    while (iter.hasNext()) {
                        String url = iter.next();
                        LOGGER.info("子系统注销地址为:{}", url);
                        try {
                            String ret = restTemplate.getForObject(url + "?token=" + token, String.class);
                            LOGGER.info("子系统注销结果:{}", ret);
                        } catch (RestClientException e) {
                            e.printStackTrace();
                            LOGGER.error("子系统注销异常, 异常信息为:{}", e.getLocalizedMessage());
                        }
                        iter.remove();
                    }
                    map.remove(token);
                }
            }
        }
    }

    public boolean hasKey(String token) {
        return map.containsKey(token);
    }

    public boolean exists(String token) {
        return map.containsKey(token) && !map.get(token).isEmpty();
    }

    public Set<String> get(String token) {
        return map.getOrDefault(token, new HashSet<>());
    }

}
