package org.yakovb.ratelimiter.exampleserver.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("")
  public String hello(@RequestHeader("user-id") String userId) {
    return "Hello, Airtasker user " + userId;
  }
}
