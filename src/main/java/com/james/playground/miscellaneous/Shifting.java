package com.james.playground.miscellaneous;

public class Shifting {
  public static void main(String[] args) {
    long now     = System.currentTimeMillis();
    long shifted = now / 100000;

    System.out.println(now);
    System.out.println(shifted);
    System.out.println(now - shifted);
  }
}
