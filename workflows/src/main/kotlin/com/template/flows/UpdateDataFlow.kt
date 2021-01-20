package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.DataContract
import com.template.states.DataState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class UpdateDataFlow(val dataString: String) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        val stateList = serviceHub.vaultService.queryBy<DataState>().states.filter { it.state.data.data == dataString }
        if (stateList.size != 1) throw FlowException("There is not 1 DataState with that ID")
        val inputState = stateList[0]


        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val command = Command(DataContract.Commands.Update(), listOf(ourIdentity).map { it.owningKey })
        val txBuilder = TransactionBuilder(notary)
        txBuilder.addInputState(inputState)
        txBuilder.addCommand(command)
        txBuilder.addOutputState(inputState.state.data)

        txBuilder.verify(serviceHub)

        val tx = serviceHub.signInitialTransaction(txBuilder)
        return subFlow(FinalityFlow(tx, listOf()))
    }
}

//@InitiatedBy(Initiator::class)
//class Responder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
//    @Suspendable
//    override fun call(): SignedTransaction {
//
//        return subFlow(ReceiveFinalityFlow(counterpartySession))
//    }
//}
