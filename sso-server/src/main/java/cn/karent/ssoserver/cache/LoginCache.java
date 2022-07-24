package cn.karent.ssoserver.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/******************************************
 * 登录缓存, 用来代替Session, 还可以使用Redis来缓存
 * @author wan
 * @date 2022.07.20 21:50
 ******************************************/
@Component
public class LoginCache {

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

    public void remove(String token) {
        if (map.containsKey(token)) {
            Set<String> urls = map.get(token);
            synchronized (urls) {
                Iterator<String> iter = urls.iterator();
                while (iter.hasNext()) {
                    String url = iter.next();
                    String ret = restTemplate.getForObject(url, String.class);
                    iter.remove();
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
