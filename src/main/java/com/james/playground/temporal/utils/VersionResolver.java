package com.james.playground.temporal.utils;

import io.temporal.common.SearchAttributeKey;
import io.temporal.workflow.Workflow;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import one.util.streamex.EntryStream;
import org.apache.commons.collections4.MapUtils;

@AllArgsConstructor
public class VersionResolver {
  private static final SearchAttributeKey<List<String>> TEMPORAL_CHANGE_VERSION =
      SearchAttributeKey.forKeywordList("TemporalChangeVersion");

  private Map<Changeable, Integer> versions;

  public static VersionResolver from(Map<Changeable, Integer> currentChangeVersions) {
    VersionResolver result = EntryStream.of(currentChangeVersions)
        .mapToValue((changeable, version) -> Workflow.getVersion(changeable.name(), Workflow.DEFAULT_VERSION, version))
        .toMapAndThen(VersionResolver::new);

    boolean hasChange = result.versions.values().stream().anyMatch(version -> version != Workflow.DEFAULT_VERSION);
    if (hasChange) {
      Workflow.upsertTypedSearchAttributes(TEMPORAL_CHANGE_VERSION.valueSet(result.toList()));
    }

    return result;
  }

  public Integer get(Changeable changeable) {
    return versions.get(changeable);
  }

  List<String> toList() {
    if (MapUtils.isEmpty(versions)) {
      return Collections.emptyList();
    }

    return EntryStream.of(versions)
        .filterValues(version -> version != Workflow.DEFAULT_VERSION)
        .mapKeyValue((changeable, version) -> changeable.name() + "-" + version)
        .toList();
  }
}
