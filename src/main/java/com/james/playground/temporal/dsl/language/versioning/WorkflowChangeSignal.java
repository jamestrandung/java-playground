package com.james.playground.temporal.dsl.language.versioning;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.james.playground.temporal.dsl.language.marketing.MarketingContextChangeSignal;

@JsonTypeInfo(
    use = Id.NAME,
    property = WorkflowChangeSignalType.PROPERTY_NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = MarketingContextChangeSignal.class, name = WorkflowChangeSignalType.MARKETING_CONTEXT),
})
public interface WorkflowChangeSignal {
}
