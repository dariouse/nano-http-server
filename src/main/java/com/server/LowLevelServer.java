package com.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LowLevelServer {
    private static final int PORT = 8080;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final boolean nagle = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(PORT);

        System.out.println("HTTP 서버 시작됨: http://localhost:" + PORT);
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
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
        String response;

        if (!nagle) {
            System.out.println("nagle true");
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            Thread.sleep(100);
            out.write("Content-Type: text/plain\r\n".getBytes());
            Thread.sleep(100);
            out.write("Content-Length: 24\r\n".getBytes());
            out.write("\r\n".getBytes());
            out.write("Hello, world! nagle true".getBytes());

            out.flush();
        } else {
            System.out.println("nagle false");
            response = """
                    HTTP/1.1 200 OK
                    Content-Type: text/plain
                    Content-Length: 24
                    
                    Hello, world! nagle false
                    """;

            out.write(response.getBytes());
            out.flush();
        }
    }
}
