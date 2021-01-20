package com.template.webserver

import com.template.flows.CreateDataFlow
import com.template.flows.UpdateDataFlow
import net.corda.client.rpc.RPCException
import net.corda.core.messaging.startFlow
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest
import kotlin.math.abs

/**
 When a node goes down, revert all traffic to other node
 Track all new requests that are getting hashed to the down node
 If there is a non-new request that hashes to the down node that we are not tracking, send back failure and make them restart
 Remove tracking of a request when it is finalized
 When other node comes back up, stop tracking new requests to the previously down node

 To do health check on webserver we continuously run a health check


 TODO: Dont do hashing at all or round-robin. Just switch between active & down nodes
 How do we necessarily know the other node is active? Do we check before redirecting traffic


 */

@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpcConnections: NodeRPCConnections) {

    companion object { private val logger = LoggerFactory.getLogger(RestController::class.java) }

    private val flowTimeoutTime: Long = 2000
    private val healthCheckSleepTime: Long = 3000

    private val proxyList = listOf(rpcConnections.proxy0, rpcConnections.proxy1)
    private val numWorkerNodes = proxyList.size
    private val availibility = arrayOf(true, true)
    private val hashingException = listOf(mutableListOf<String>(), mutableListOf<String>())


    @PostMapping(value = ["/runFlowA"], produces = ["text/plain"])
    private fun runFlowA(request: HttpServletRequest): String {
        val data = request.getParameter("data")
        var bucketIndex = getHashBucket(data)

        println("Using node " + bucketIndex)

        if (!availibility[bucketIndex]) {
            startFlowOnOtherNode(data)
            return ""
        }

        try {
            proxyList[bucketIndex].startFlow(::CreateDataFlow, data).returnValue.get(flowTimeoutTime, TimeUnit.MILLISECONDS)
        }
        catch (e: RPCException) {
            println("Flow time out!")
            e.printStackTrace()
            availibility[bucketIndex] = false
            startHealthCheck(bucketIndex)
            // TODO: Retry this request on secondary node
        }

        return bucketIndex.toString()
    }

    @PostMapping(value = ["/runFlowB"], produces = ["text/plain"])
    private fun runFlowB(request: HttpServletRequest): String {

        val data = request.getParameter("data")
        val bucketIndex = getHashBucket(data)
        // TODO: Check hash exceptions........

        if (hashingException[bucketIndex].contains("data")) {
            // There was a hashing exception, this state needs to continue it's updates on the other node

        }


        proxyList[bucketIndex].startFlow(::UpdateDataFlow, data).returnValue.get(flowTimeoutTime, TimeUnit.MILLISECONDS)

        return "Define an endpoint here."
    }


    private fun startFlowOnOtherNode(data: String) {
        val originalHash = getHashBucket(data)
        val bucketIndex = if (originalHash==0) 1 else 0 // We want to use the opposite of the hash function
        proxyList[bucketIndex].startFlow(::CreateDataFlow, data).returnValue.get(flowTimeoutTime, TimeUnit.MILLISECONDS)

        // TODO: Track hashing exceptions for when the other node starts up
        hashingException[originalHash].add(data)
    }


    private fun startHealthCheck(proxyNumber: Int) {
        println("Starting health check " + proxyNumber.toString())

        val executorService = Executors.newSingleThreadExecutor()
        executorService.execute {
            while (!healthCheck(proxyNumber)) {
                Thread.sleep(healthCheckSleepTime)
            }
            availibility[proxyNumber] = true
            println(proxyNumber.toString() + " is back up!")
        }
        executorService.shutdown()
    }

    private fun healthCheck(proxyNumber: Int): Boolean {
        println("Running health check...")
        val proxy = proxyList[proxyNumber]
        return try {
            proxy.nodeInfo()
            true
        }
        catch (e: RPCException) {
            println("Node is still down!")
            false
        }
    }






    private fun getHashBucket(data: String): Int {
        return abs(data.hashCode() % numWorkerNodes)
    }




}
















// Dont do round robin, just switchover everything......
// and cancel what currently exists...... how would we do that..



// What if calls after initiate, also pass in the worker node number

// Only in case of detected outage?
// After initiate, store which node it's on in main memory
// Remove from hashmap at fund







///**
// * Define your API endpoints here.
// */
//@RestController
//@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
//class Controller(rpc: NodeRPCConnection) {
//
//    companion object {
//        private val logger = LoggerFactory.getLogger(RestController::class.java)
//    }
//
//    private val proxy = rpc.proxy
//
//    @GetMapping(value = ["/runFlow"], produces = ["text/plain"])
//    private fun runFlow(request: HttpServletRequest): String {
//
//        val data = request.getParameter("data")
//        val party = request.getParameter("party")
//
//
//
//
//
////        val networkHostList = listOf(NetworkHostAndPort("host1", 10001), NetworkHostAndPort("host2", 10002))
////        val multiRpcClient = MultiRPCClient(haAddressPool = networkHostList, rpcOpsClass = CordaRPCOps::class.java, username = "username", password = "password")
////        multiRpcClient.start()
//
//
//
//
//
//
//        //proxy.startTrackedFlow(::Initiator, "My data", PARTY)
//
//
//
//
//
//        return "Define an endpoint here."
//    }
//}