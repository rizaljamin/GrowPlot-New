package com.growplot.farm;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
public class RtspCredentialVerifierTest {
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    private static void basic(int finalStatus, RtspCredentialVerifier.Result expected) throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            Thread worker = new Thread(() -> {
                try {
                    for (int i = 0; i < 2; i++) try (Socket socket = server.accept()) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                        String line; boolean credentials = false;
                        while ((line=reader.readLine())!=null && !line.isEmpty()) if (line.equals("Authorization: Basic dXNlcjpwYXNz")) credentials=true;
                        if (i==1 && !credentials) throw new AssertionError("Saved credentials were not used");
                        int status = i==0?401:finalStatus;
                        socket.getOutputStream().write(("RTSP/1.0 "+status+" Status\r\nWWW-Authenticate: Basic realm=\"camera\"\r\nContent-Length: 0\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) { throw new RuntimeException(e); }
            });worker.start();
            check(RtspCredentialVerifier.verify("127.0.0.1", server.getLocalPort(), "user", "pass")==expected);
            worker.join(6000);check(!worker.isAlive());
        }
    }
    public static void main(String[] args) throws Exception {
        basic(200,RtspCredentialVerifier.Result.ACCEPTED);
        basic(401,RtspCredentialVerifier.Result.REJECTED);
        basic(503,RtspCredentialVerifier.Result.UNKNOWN);
        try(ServerSocket closed=new ServerSocket(0)){int port=closed.getLocalPort();closed.close();check(RtspCredentialVerifier.verify("127.0.0.1",port,"user","pass")==RtspCredentialVerifier.Result.UNKNOWN);}
        java.lang.reflect.Method auth=RtspCredentialVerifier.class.getDeclaredMethod("authorization",String.class,String.class,String.class,String.class);auth.setAccessible(true);
        String digest=(String)auth.invoke(null,"Digest realm=\"camera\", nonce=\"123\", qop=\"auth,auth-int\", algorithm=MD5","rtsp://camera/stream1","user","pass");
        check(digest.contains("qop=auth")&&digest.contains("response=\"")&&digest.contains("cnonce=\""));
        check(auth.invoke(null,"Digest realm=\"camera\", nonce=\"123\", algorithm=SHA-256","rtsp://camera/stream1","user","pass")==null);
        System.out.println("PASS: accepted/rejected login, server/network failure, Digest challenge and unsupported auth.");
    }
}
