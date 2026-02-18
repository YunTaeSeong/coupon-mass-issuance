package com.example.couponapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() throws InterruptedException {
        // 초당 2건 처리 * N(서버에서 동시에 처리 가능한 수 = 톰캣에서 쓰레드 풀을 통해 요청을 처리(max = 200))
        // => 400
        Thread.sleep(500);
        return "hello!";
    }
}
