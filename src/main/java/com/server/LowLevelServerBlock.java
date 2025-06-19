package com.server;

import util.ThreadUtil;

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

public class LowLevelServerBlock {
    private static final int PORT = 8080;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final boolean nagle = true;

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();

        // blocking 모드로 accept 대기
        serverChannel.configureBlocking(true);

        serverChannel.bind(new InetSocketAddress(PORT));


        System.out.println("HTTP 서버 시작됨: http://localhost:" + PORT);

        while (true) {
            try {
                System.out.println("여기서 Blocking 시작~~~");
                SocketChannel clientChannel = serverChannel.accept(); // accept blocking
                System.out.println("연결되면 Blocking 끝내기~~~");

                Socket clientSocket = clientChannel.socket();
                clientSocket.setTcpNoDelay(nagle);

                executor.submit(() -> {
                    try {
                        handleClient(clientSocket);
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

    private static void handleClient(Socket clientSocket) throws IOException, InterruptedException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        String requestLine = in.readLine();
        System.out.println("요청: " + requestLine);

        // 문제1. nagle 알고리즘에 의해서 Client 에서 true, false 차이를 체감할 수 있을까?
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
        }
    }
}
