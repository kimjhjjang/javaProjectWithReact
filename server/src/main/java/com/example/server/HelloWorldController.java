package com.example.server;

import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

  @GetMapping("api/hello")
  public List<String> hello() {
    return Arrays.asList("고은서 테스트", "수정 테스트");
  }
}
