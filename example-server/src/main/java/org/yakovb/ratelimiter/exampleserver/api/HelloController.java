package org.yakovb.ratelimiter.exampleserver.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("/{userId}")
  public String hello(@PathVariable String userId) {
    return "Hello, Airtasker user " + userId;
  }
}
