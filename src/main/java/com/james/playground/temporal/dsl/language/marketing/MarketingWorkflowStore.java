package com.james.playground.temporal.dsl.language.marketing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MarketingWorkflowStore {
  public static final ObjectMapper OBJECT_MAPPER;
  private static final Map<String, MarketingWorkflowDefinition> CACHE;
  private static MarketingWorkflowStore INSTANCE;

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OBJECT_MAPPER.registerModule(new JavaTimeModule());

    try {
      // 1. Initial version
      //      Resource resource = new ClassPathResource("workflow_definition_1.json");
      // 2. Updated delay duration + printed text
      //      Resource resource = new ClassPathResource("workflow_definition_2.json");
      // 3. Delete a node
      //      Resource resource = new ClassPathResource("workflow_definition_3.json");
      // 4. Add a new node
      //      Resource resource = new ClassPathResource("workflow_definition_4.json");

      // 1. Long delay of 3600 seconds
      Resource resource = new ClassPathResource("long_delay.json");
      // 2. Shorten the delay
      //      Resource resource = new ClassPathResource("long_delay_shorten.json");
      // 3. Delete the delay node
      //      Resource resource = new ClassPathResource("long_delay_deleted.json");

      // Branching
      //      Resource resource = new ClassPathResource("random_distribution.json");
      //      Resource resource = new ClassPathResource("switch.json");

      //      Resource resource = new ClassPathResource("mixing_delay_types.json");

      Resource resource1 = new ClassPathResource("long_delay.json");
      Resource resource2 = new ClassPathResource("workflow_definition_1.json");

      MarketingWorkflowDefinition definition = OBJECT_MAPPER.readValue(resource.getInputStream(), MarketingWorkflowDefinition.class);
      MarketingWorkflowDefinition definition1 = OBJECT_MAPPER.readValue(resource1.getInputStream(), MarketingWorkflowDefinition.class);
      MarketingWorkflowDefinition definition2 = OBJECT_MAPPER.readValue(resource2.getInputStream(), MarketingWorkflowDefinition.class);

      CACHE = new HashMap<>(
          Map.of(
              "workflowDefinitionId", definition,
              "workflowDefinitionId1", definition1,
              "workflowDefinitionId2", definition2
          )
      );

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static MarketingWorkflowStore getInstance() {
    return INSTANCE;
  }

  @PostConstruct
  void init() {
    INSTANCE = this;
  }

  public MarketingWorkflowDefinition findWorkflowDefinition(String workflowDefinitionId) {
    return CACHE.getOrDefault(workflowDefinitionId, MarketingWorkflowDefinition.BLANK);
  }

  public void updateWorkflowDefinition(String workflowDefinitionId, MarketingWorkflowDefinition definition) {
    CACHE.put(workflowDefinitionId, definition);
  }
}
