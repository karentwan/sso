package cn.karent.client2.config;

import cn.karent.client2.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/******************************************
 * 拦截器配置信息
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
                .addPathPatterns("/")
                .excludePathPatterns("/index.html");
    }
}
