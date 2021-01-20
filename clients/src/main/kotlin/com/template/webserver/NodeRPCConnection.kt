package com.template.webserver

import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


private const val CORDA_NODE_HOST_0 = "config.rpc.host.0"
private const val CORDA_RPC_PORT_0 = "config.rpc.port.0"
private const val CORDA_NODE_HOST_1 = "config.rpc.host.1"
private const val CORDA_RPC_PORT_1 = "config.rpc.port.1"
private const val CORDA_USER_NAME = "config.rpc.username"
private const val CORDA_USER_PASSWORD = "config.rpc.password"

/**
 * Wraps an RPC connection to a Corda node.
 *
 * The RPC connection is configured using command line arguments.
 *
 * @param host The host of the node we are connecting to.
 * @param rpcPort The RPC port of the node we are connecting to.
 * @param username The username for logging into the RPC client.
 * @param password The password for logging into the RPC client.
 * @property proxy The RPC proxy.
 */
@Component
open class NodeRPCConnections(
        @Value("\${$CORDA_NODE_HOST_0}") private val host0: String,
        @Value("\${$CORDA_RPC_PORT_0}") private val rpcPort0: Int,
        @Value("\${$CORDA_NODE_HOST_1}") private val host1: String,
        @Value("\${$CORDA_RPC_PORT_1}") private val rpcPort1: Int,
        @Value("\${$CORDA_USER_NAME}") private val username: String,
        @Value("\${$CORDA_USER_PASSWORD}") private val password: String
       ): AutoCloseable {

    lateinit var rpcConnection0: CordaRPCConnection
        private set
    lateinit var proxy0: CordaRPCOps
        private set

    lateinit var rpcConnection1: CordaRPCConnection
        private set
    lateinit var proxy1: CordaRPCOps
        private set

    @PostConstruct
    fun initialiseNodeRPCConnections() {
        val rpcAddress0 = NetworkHostAndPort(host0, rpcPort0)
        val rpcClient0 = CordaRPCClient(rpcAddress0)
        rpcConnection0 = rpcClient0.start(username, password)
        proxy0 = rpcConnection0.proxy

        val rpcAddress1 = NetworkHostAndPort(host1, rpcPort1)
        val rpcClient1 = CordaRPCClient(rpcAddress1)
        rpcConnection1 = rpcClient1.start(username, password)
        proxy1 = rpcConnection1.proxy
    }

    @PreDestroy
    override fun close() {
        rpcConnection0.notifyServerAndClose()
        rpcConnection1.notifyServerAndClose()
    }
}