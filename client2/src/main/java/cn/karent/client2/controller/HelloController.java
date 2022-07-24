package cn.karent.client2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/******************************************
 *
 * @author wan
 * @date 2022.07.24 22:04
 ******************************************/
@Controller
public class HelloController {

    private final static Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/hello")
    public String hello() {
        LOGGER.info("hello");
        return "hello";
    }

}
