package com.james.playground.miscellaneous;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Hashing {
  public static void main(String[] args) {
    var map = Map.of(
        "key1", 123L,
        "key2", 456L,
        "key3", 789L
    );

    HashCodeBuilder mapHash = new HashCodeBuilder();
    mapHash.append(map);
    System.out.println(mapHash.toHashCode());

    var hashMap = new HashMap<>(map);

    HashCodeBuilder hashMapHash = new HashCodeBuilder();
    hashMapHash.append(hashMap);
    System.out.println(hashMapHash.toHashCode());

    hashMap.hashCode();

    var linkedMap = new LinkedHashMap<>(map);

    HashCodeBuilder linkedMapHash = new HashCodeBuilder();
    linkedMapHash.append(linkedMap);
    System.out.println(linkedMapHash.toHashCode());
  }
}
