package com.james.playground.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DummyService {
  public void say(String text) {
    log.info("Saying: {}", text);
  }
}
