package com.pit.core.net;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author gy
 */
@Slf4j
public class PortUtil {
    private final static int PORT_INT_UPPER = 65535;

    /**
     * 找可用的端口
     *
     * @param defaultPort
     * @return
     */
    public static int findAvailablePort(int defaultPort) throws IOException {
        int portTmp = defaultPort;
        while (portTmp < PORT_INT_UPPER) {
            if (!isPortUsed(portTmp)) {
                return portTmp;
            } else {
                portTmp++;
            }
        }
        portTmp = defaultPort--;
        while (portTmp > 0) {
            if (!isPortUsed(portTmp)) {
                return portTmp;
            } else {
                portTmp--;
            }
        }
        throw new RuntimeException("no available port.");
    }

    /**
     * 检查端口是否可用
     *
     * @param port
     * @return
     */
    public static boolean isPortUsed(int port) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            return false;
        } catch (IOException e) {
            log.info("port[{}] is in use.", port);
            return true;
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}
