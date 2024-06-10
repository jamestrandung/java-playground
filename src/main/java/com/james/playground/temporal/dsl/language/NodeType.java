package com.james.playground.temporal.dsl.language;

public class NodeType {
  public static final String PROPERTY_NAME = "@type";
  // These string values must NEVER be modified
  // as FE has a direct coupling with them.
  public static final String TRANSIT = "TRANSIT";
  public static final String PRINTER = "PRINTER";
  public static final String DELAY = "DELAY";
  public static final String RANDOM_DISTRIBUTION = "RANDOM_DISTRIBUTION";
  public static final String BRANCH = "BRANCH";
}
