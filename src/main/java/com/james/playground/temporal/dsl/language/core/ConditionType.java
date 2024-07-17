package com.james.playground.temporal.dsl.language.core;

public enum ConditionType {
  GROUP_MEMBERSHIP("GROUP_MEMBERSHIP");

  public final String value;

  ConditionType(String value) {
    this.value = value;
  }

  public static class Constants {
    public static final String PROPERTY_NAME = "@type";
    public static final String GROUP_MEMBERSHIP_VALUE = "GROUP_MEMBERSHIP";
  }
}
