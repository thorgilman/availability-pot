//package com.template.webserver
//
//import net.corda.client.rpc.CordaRPCClient
//import net.corda.client.rpc.CordaRPCConnection
//import net.corda.client.rpc.RPCConnection
//import net.corda.client.rpc.ext.MultiRPCClient
//import net.corda.core.messaging.CordaRPCOps
//import net.corda.core.utilities.NetworkHostAndPort
//import org.hibernate.validator.internal.util.Contracts.assertNotNull
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.stereotype.Component
//import java.util.concurrent.CompletableFuture
//import javax.annotation.PostConstruct
//import javax.annotation.PreDestroy
//
//private const val CORDA_USER_NAME = "config.rpc.username"
//private const val CORDA_USER_PASSWORD = "config.rpc.password"
//private const val CORDA_NODE_HOST = "config.rpc.host"
//private const val CORDA_RPC_PORT = "config.rpc.port"
//
///**
// * Wraps an RPC connection to a Corda node.
// *
// * The RPC connection is configured using command line arguments.
// *
// * @param host The host of the node we are connecting to.
// * @param rpcPort The RPC port of the node we are connecting to.
// * @param username The username for logging into the RPC client.
// * @param password The password for logging into the RPC client.
// * @property proxy The RPC proxy.
// */
//@Component
//open class MultiNodeRPCConnection(
//        @Value("\${$CORDA_NODE_HOST}") private val hostList: List<String>,
//        @Value("\${$CORDA_USER_NAME}") private val username: String,
//        @Value("\${$CORDA_USER_PASSWORD}") private val password: String,
//        @Value("\${$CORDA_RPC_PORT}") private val rpcPortList: List<Int>): AutoCloseable {
//
//    lateinit var rpcConnection: CordaRPCConnection
//        private set
//    lateinit var proxy: CordaRPCOps
//        private set
//
//    @PostConstruct
//    fun initialiseNodeRPCConnection() {
//
//
//        val networkHostList = mutableListOf<NetworkHostAndPort>()
//        for (i in hostList.indices) {
//            val host = hostList[i]
//            val port = rpcPortList[i]
//            networkHostList.add(NetworkHostAndPort(host, port))
//        }
//
//        val client = MultiRPCClient(haAddressPool = networkHostList, rpcOpsClass = CordaRPCOps::class.java, username = username, password = password)
//        client.use {
//            val connFuture: CompletableFuture<RPCConnection<CordaRPCOps>> = client.start()
//            val conn: RPCConnection<CordaRPCOps> = connFuture.get()
//
//
//            conn.use {
//                assertNotNull(it.proxy.nodeInfo())
//            }
//
//            proxy = conn.proxy
//        }
//
//
//
//
//
////        val rpcClient = CordaRPCClient(rpcPortList[0])
////
////        val rpcConnection = rpcClient.start(username, password)
////        proxy = rpcConnection.proxy
//    }
//
//    @PreDestroy
//    override fun close() {
//        rpcConnection.notifyServerAndClose()
//    }
//}