package com.james.playground.temporal.properties;

import com.james.playground.utils.FormatUtils;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Properties {
  private Map<String, String> raw;

  public <T> Set<T> getSet(String key) {
    // not working
    //    return FormatUtils.fromJsonString(this.raw.get(key), new TypeReference<Set<T>>() {
    //    });

    return FormatUtils.fromJsonString(this.raw.get(key), Set.class, Long.class);
  }
}
