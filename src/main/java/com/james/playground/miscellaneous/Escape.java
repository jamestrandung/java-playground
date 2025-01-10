package com.james.playground.miscellaneous;

import org.springframework.web.util.HtmlUtils;

public class Escape {
  public static void main(String[] args) {
    String text = "Mùa lễ này, sự kiện \"Vé đến Paradise\" của chúng tôi có phần thưởng lớn hơn 3 triệu USDT";
    System.out.println(HtmlUtils.htmlEscape(text));
    System.out.println(HtmlUtils.htmlEscape(text, "UTF-8"));
  }
}
