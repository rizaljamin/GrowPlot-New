package com.growplot.farm;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.*;

/** Checks authentication only; an unreachable camera is not a rejected password. */
final class RtspCredentialVerifier {
    enum Result { ACCEPTED, REJECTED, UNKNOWN }
    private static final class Reply {
        int status;
        String challenge = "";
    }
    static Result verify(String host, int port, String username, String password) {
        try {
            String uri = "rtsp://" + host + ":" + port + "/stream1";
            Reply first = request(host, port, uri, "");
            if (first.status >= 200 && first.status < 300) return Result.ACCEPTED;
            if (first.status != 401) return Result.UNKNOWN;
            String auth = authorization(first.challenge, uri, username, password);
            if (auth == null) return Result.UNKNOWN;
            Reply second = request(host, port, uri, auth);
            if (second.status >= 200 && second.status < 300) return Result.ACCEPTED;
            if (second.status == 401) {
                if (second.challenge.toLowerCase(Locale.US).contains("stale=true")
                        || second.challenge.toLowerCase(Locale.US).contains("stale=\"true\"")) return Result.UNKNOWN;
                return Result.REJECTED;
            }
            return Result.UNKNOWN;
        } catch (Exception unavailable) { return Result.UNKNOWN; }
    }
    private static Reply request(String host, int port, String uri, String authorization) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 2500);
            socket.setSoTimeout(2500);
            String message = "DESCRIBE " + uri + " RTSP/1.0\r\nCSeq: 1\r\nAccept: application/sdp\r\n"
                    + (authorization.isEmpty() ? "" : "Authorization: " + authorization + "\r\n") + "\r\n";
            socket.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String line = reader.readLine();
            if (line == null || !line.startsWith("RTSP/1.0 ")) throw new IOException("No RTSP response");
            Reply reply = new Reply(); reply.status = Integer.parseInt(line.split(" ")[1]);
            int bytes = 0;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                bytes += line.length(); if (bytes > 16384) throw new IOException("RTSP headers too large");
                if (line.toLowerCase(Locale.US).startsWith("www-authenticate:")) reply.challenge = line.substring(line.indexOf(':') + 1).trim();
            }
            return reply;
        }
    }
    private static String authorization(String challenge, String uri, String user, String password) throws Exception {
        if (user.contains("\r") || user.contains("\n") || password.contains("\r") || password.contains("\n")) return null;
        if (challenge.toLowerCase(Locale.US).startsWith("basic")) {
            return "Basic " + base64((user + ":" + password).getBytes(StandardCharsets.UTF_8));
        }
        if (!challenge.toLowerCase(Locale.US).startsWith("digest")) return null;
        Map<String,String> values = new HashMap<>();
        Matcher matcher = Pattern.compile("([a-zA-Z]+)\\s*=\\s*(?:\"([^\"]*)\"|([^,\\s]+))").matcher(challenge);
        while (matcher.find()) values.put(matcher.group(1).toLowerCase(Locale.US), matcher.group(2) == null ? matcher.group(3) : matcher.group(2));
        String realm = values.get("realm"), nonce = values.get("nonce");
        if (realm == null || nonce == null) return null;
        String algorithm = values.getOrDefault("algorithm", "MD5");
        if (!algorithm.equalsIgnoreCase("MD5") && !algorithm.equalsIgnoreCase("MD5-sess")) return null;
        String qop = values.get("qop"), cnonce = UUID.randomUUID().toString().replace("-", "");
        if (qop != null && !Arrays.asList(qop.toLowerCase(Locale.US).split("\\s*,\\s*")).contains("auth")) return null;
        String ha1 = md5(user + ":" + realm + ":" + password);
        if (algorithm.equalsIgnoreCase("MD5-sess")) ha1 = md5(ha1 + ":" + nonce + ":" + cnonce);
        String response = md5(ha1 + ":" + nonce + ":" + (qop == null ? "" : "00000001:" + cnonce + ":auth:") + md5("DESCRIBE:" + uri));
        String result = "Digest username=" + quote(user) + ", realm=" + quote(realm) + ", nonce=" + quote(nonce)
                + ", uri=" + quote(uri) + ", response=" + quote(response) + ", algorithm=" + algorithm;
        if (qop != null) result += ", qop=auth, nc=00000001, cnonce=" + quote(cnonce);
        else if (algorithm.equalsIgnoreCase("MD5-sess")) result += ", cnonce=" + quote(cnonce);
        if (values.containsKey("opaque")) result += ", opaque=" + quote(values.get("opaque"));
        return result;
    }
    private static String base64(byte[] bytes) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < bytes.length; i += 3) {
            int value = (bytes[i] & 255) << 16;
            if (i + 1 < bytes.length) value |= (bytes[i + 1] & 255) << 8;
            if (i + 2 < bytes.length) value |= bytes[i + 2] & 255;
            out.append(alphabet.charAt((value >>> 18) & 63)).append(alphabet.charAt((value >>> 12) & 63));
            out.append(i + 1 < bytes.length ? alphabet.charAt((value >>> 6) & 63) : '=');
            out.append(i + 2 < bytes.length ? alphabet.charAt(value & 63) : '=');
        }
        return out.toString();
    }
    private static String quote(String value) { return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
    private static String md5(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder(); for (byte b : digest) text.append(String.format(Locale.US, "%02x", b & 255)); return text.toString();
    }
}
