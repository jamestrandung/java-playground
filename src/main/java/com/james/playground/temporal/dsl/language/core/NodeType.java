package com.james.playground.temporal.dsl.language.core;

import lombok.Getter;

@Getter
public enum NodeType {
  // These string values must NEVER be modified
  // as FE has a direct coupling with them.
  TRANSIT(Constants.TRANSIT_VALUE),
  PRINTER(Constants.PRINTER_VALUE),
  DELAY(Constants.DELAY_VALUE),
  RANDOM_DISTRIBUTION(Constants.RANDOM_DISTRIBUTION_VALUE),
  SWITCH(Constants.SWITCH_VALUE);
  
  public final String value;

  NodeType(String value) {
    this.value = value;
  }

  public static class Constants {
    public static final String PROPERTY_NAME = "@type";
    public static final String TRANSIT_VALUE = "TRANSIT";
    public static final String PRINTER_VALUE = "PRINTER";
    public static final String DELAY_VALUE = "DELAY";
    public static final String RANDOM_DISTRIBUTION_VALUE = "RANDOM_DISTRIBUTION";
    public static final String SWITCH_VALUE = "SWITCH";
  }
}
