package com.james.playground.temporal

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import spock.lang.Specification

class DummyTest extends Specification {
    def workflowClientOptions = GroovyMock(WorkflowClientOptions) {
        getNamespace() >> "namespace"
        getIdentity() >> "identity"
    }
    def workflowClient = Mock(WorkflowClient) {
        getOptions() >> workflowClientOptions
    }

    def "test dummy"() {
        expect:
        workflowClient.getOptions().getNamespace() == "namespace"
    }
}
