package com.james.playground.temporal.moneytransfer.workflows

import com.james.playground.temporal.BaseWorkflowSpecification
import com.james.playground.temporal.moneytransfer.activities.AccountActivity
import com.james.playground.temporal.moneytransfer.dto.TransactionDetails
import org.springframework.core.io.ClassPathResource

class MoneyTransferWorkflowImplTest extends BaseWorkflowSpecification<MoneyTransferWorkflow> {
    AccountActivity accountActivity = Mock(AccountActivity)

    @Override
    List<Object> getActivityImplementationsToRegister() {
        return List.of(accountActivity)
    }

    @Override
    Class<?> getWorkflowImplementationTypeToRegister() {
        return MoneyTransferWorkflowImpl.class
    }

    @Override
    Class<MoneyTransferWorkflow> getWorkflowTypeToBuildStub() {
        return MoneyTransferWorkflow.class
    }

    def "test transfer"() {
        given:
        def details = TransactionDetails.builder()
                .referenceId("referenceId")
                .sourceAccountId("sourceId")
                .destinationAccountId("destinationId")
                .amountToTransfer(1000)
                .withdrawDelayInSeconds(1000)
                .shouldSucceed(true)
                .build()

        when:
        def result = workflow.transfer(details)

        then:
        1 * accountActivity.withdraw(details.referenceId, details.sourceAccountId, details.amountToTransfer, details.withdrawDelayInSeconds)
        1 * accountActivity.depositV2(details.referenceId, details.destinationAccountId, details.amountToTransfer, details.shouldSucceed)

        then:
        result == "TRANSFER_COMPLETED_" + details.amountToTransfer
    }

    def "test determinism"() {
        given:
        def resource = new ClassPathResource("history_encoded.json")

        when:
        this.replayWorkflowExecution(resource.getFile())

        then:
        noExceptionThrown()
    }
}
