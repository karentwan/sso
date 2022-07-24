package cn.karent.client1.config;

import cn.karent.client1.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/******************************************
 *
 * @author wan
 * @date 2022.07.24 21:12
 ******************************************/
@Configuration
public class IntercetorConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/*")  // 拦截所有
                .excludePathPatterns("/index.html");  // 不拦截此路劲的请求
    }


}
