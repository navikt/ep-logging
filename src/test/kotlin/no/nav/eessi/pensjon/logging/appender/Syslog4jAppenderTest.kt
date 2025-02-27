package no.nav.eessi.pensjon.logging.appender

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.productivity.java.syslog4j.server.SyslogServer
import org.productivity.java.syslog4j.server.impl.event.printstream.PrintStreamSyslogServerEventHandler
import org.productivity.java.syslog4j.server.impl.net.tcp.TCPNetSyslogServerConfig
import org.productivity.java.syslog4j.server.impl.net.tcp.ssl.SSLTCPNetSyslogServerConfig
import org.productivity.java.syslog4j.server.impl.net.udp.UDPNetSyslogServerConfig
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@ExtendWith(MockKExtension::class)
class Syslog4jAppenderTest {
    private lateinit var serverStream: ByteArrayOutputStream

    @BeforeEach
    fun setUp() {
        serverStream = ByteArrayOutputStream()
        val ps = PrintStream(serverStream)

        val tcpNetSyslogServerConfig = TCPNetSyslogServerConfig(45553)
        tcpNetSyslogServerConfig.addEventHandler(PrintStreamSyslogServerEventHandler(ps))

        val udpNetSyslogServerConfig = UDPNetSyslogServerConfig(45553)
        udpNetSyslogServerConfig.addEventHandler(PrintStreamSyslogServerEventHandler(ps))

        val ssltcpNetSyslogServerConfig = SSLTCPNetSyslogServerConfig()
        ssltcpNetSyslogServerConfig.port = 45554
        ssltcpNetSyslogServerConfig.addEventHandler(PrintStreamSyslogServerEventHandler(ps))
        ssltcpNetSyslogServerConfig.keyStore = this::class.java.classLoader.getResource("test-keystore.jks")!!.file
        ssltcpNetSyslogServerConfig.keyStorePassword = "password"
        ssltcpNetSyslogServerConfig.trustStore = this::class.java.classLoader.getResource("test-keystore.jks")!!.file
        ssltcpNetSyslogServerConfig.trustStorePassword = "password"

        SyslogServer.createThreadedInstance("testTcp", tcpNetSyslogServerConfig)
        SyslogServer.createThreadedInstance("testUdp", udpNetSyslogServerConfig)
        SyslogServer.createThreadedInstance("testTls", ssltcpNetSyslogServerConfig)
    }

    @AfterEach
    fun tearDown() {
        SyslogServer.shutdown()
    }

    @Test
    @Throws(JoranException::class, InterruptedException::class)
    fun testUdpSender() {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val configurator = JoranConfigurator()
        configurator.context = context
        context.reset()
        configurator.doConfigure(this::class.java.classLoader.getResourceAsStream("logback-syslog4j-udp.xml"))

        val logger = context.getLogger("test-udp")
        logger.info("test message over udp")

        context.stop()
        Thread.sleep(100)

        val serverData = serverStream.toString()
        assertTrue(serverData.contains("test message over udp"), "Server received: $serverData")
    }

    @Test
    @Throws(JoranException::class, InterruptedException::class)
    fun testTcpSender() {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val configurator = JoranConfigurator()
        configurator.context = context
        context.reset()
        configurator.doConfigure(this::class.java.classLoader.getResourceAsStream("logback-syslog4j-tcp.xml"))

        val logger = context.getLogger("test-tcp")
        logger.info("test message over tcp")

        context.stop()
        Thread.sleep(100)

        val serverData = serverStream.toString()
        assertTrue(serverData.contains("test message over tcp"), "Server received: $serverData")
    }

    @Test
    @Throws(JoranException::class, InterruptedException::class)
    fun testTlsSender() {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val configurator = JoranConfigurator()
        configurator.context = context
        context.reset()
        configurator.doConfigure(this::class.java.classLoader.getResourceAsStream("logback-syslog4j-tls.xml"))

        val logger = context.getLogger("test-tls")
        logger.info("test message over tls")

        context.stop()
        Thread.sleep(100)

        val serverData = serverStream.toString()
        assertTrue(serverData.contains("test message over tls"), "Server received: $serverData")
    }
}