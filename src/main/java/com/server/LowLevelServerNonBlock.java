package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.ThreadUtil;

public class LowLevelServerNonBlock {
    private static final int PORT = 8080;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final boolean nagle = true;

    public static void main(String[] args) throws IOException {
        long count = 0;

        ServerSocketChannel serverChannel = ServerSocketChannel.open();

        /*
        TIME_WAIT 상태의 포트가 있어도 서버 재시작이 가능하도록 허용하는 옵션입니다.
        서버(50000) 가 죽은 후 같은 포트(예: 50000)를 다시 client 가 사용후 close 하면 TIME_WAIT 상태일텐데
        이때 서버가 다시 50000 으로 LISTEN 하기 위해 TIME_WAIT 상태로 OS에 남아 있어도 재사용을 허용해주는 옵션입니다.
         */
        serverChannel.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, false);

        // Non-blocking 모드로 accept 에서 할게 없으면 지나감
        serverChannel.configureBlocking(false);

        serverChannel.bind(new InetSocketAddress(PORT));

        System.out.println("HTTP 서버 시작됨: http://localhost:" + PORT);

        while (true) {
            try {
                SocketChannel clientChannel = serverChannel.accept(); // accept Non-blocking
                if (clientChannel == null) {
                    ThreadUtil.sleep(1500); // CPU 낭비 방지
                    System.out.println("Non Block Continue count: " + count++);

                    continue;
                }

                Socket clientSocket = clientChannel.socket();
                clientSocket.setTcpNoDelay(nagle); // false 마지막으로 전송한 패킷에 대한 ACK가 도착하기 전까지, 새로운 작은 패킷은 보내지 않고 소켓 버퍼에 모아둔다.
                clientSocket.setSoLinger(true, 0); // true: 서버가 먼저 끊어도 TIME_WAIT 이 안남는다.

                ThreadUtil.sleep(1000);
                executor.submit(() -> {
                    try {
                        System.out.println("Pass handleClient");
                        handleClient(clientSocket, serverChannel);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleClient(Socket clientSocket, ServerSocketChannel serverChannel) throws IOException, InterruptedException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        String requestLine = in.readLine();
        System.out.println("Request: " + requestLine);

        if (nagle) {
            System.out.println("nagle true");
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Content-Type: text/plain\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Content-Length: 140\r\n".getBytes());
            out.write("\r\n".getBytes());
            out.write("Hello, world! nagle true\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Hello, world! nagle true\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Hello, world! nagle true\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Hello, world! nagle true\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Hello, world! nagle true\r\n".getBytes());

            out.flush();
//            serverChannel.close();
            clientSocket.shutdownOutput();

        } else {
            System.out.println("nagle false");
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Content-Type: text/plain\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Content-Length: 140\r\n".getBytes());
            out.write("\r\n".getBytes());
            out.write("Hello, world! nagle false\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Hello, world! nagle false\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Hello, world! nagle false\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Hello, world! nagle false\r\n".getBytes());
            ThreadUtil.sleep(1000);
            out.write("Hello, world! nagle false\r\n".getBytes());

            out.flush();
//            serverChannel.close();
            clientSocket.shutdownOutput();
        }
    }
}
