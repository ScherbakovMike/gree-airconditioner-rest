package com.gree.airconditioner.service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Base64;

/**
 * Gree LAN client with ECB+GCM support (UDP/7000).
 *
 * Protocol gist:
 *  - Discover: send {"t":"scan"} broadcast; replies have Base64 "pack" encrypted with the GENERIC key (AES-128-ECB).
 *  - Bind: send {"t":"bind"} (ECB) to receive device-specific 16-char key.
 *  - Control/status: encrypt inner JSON with the device key. Older FW: AES-ECB; newer FW: AES-GCM.
 *
 * Auto-negotiation after binding:
 *  - Try a small status request using ECB; if it fails/timeouts or cannot decrypt, try GCM.
 *  - Cache the working mode per device.
 */
public class GreeLanClient {

  // === Protocol constants ===
  public static final int GREE_UDP_PORT = 7000;
  public static final String BROADCAST_IP = "255.255.255.255";

  // Generic AES-128 key (discovery + bind)
  private static final String GENERIC_KEY = "a3K8Bx%2r8Y7#xDh";

  // GCM parameters
  private static final int GCM_TAG_BITS = 128;   // 16-byte auth tag
  private static final int GCM_IV_LEN   = 12;    // 12-byte nonce

  private final int recvTimeoutMs;
  private final AtomicInteger seq = new AtomicInteger(1);
  private final SecureRandom rng = new SecureRandom();

  // Discovered devices by MAC
  private final Map<String, Device> devicesByMac = new ConcurrentHashMap<>();

  public GreeLanClient(int recvTimeoutMs) {
    this.recvTimeoutMs = recvTimeoutMs;
  }

  // === Device record ===
  public static class Device {
    public final InetAddress ip;
    public final String mac;
    public final String name;
    public final String version;  // firmware string from scan (if provided)
    public String deviceKey;      // learned on bind
    public Enc enc = Enc.UNKNOWN; // chosen encryption for control

    public Device(InetAddress ip, String mac, String name, String version) {
      this.ip = ip; this.mac = mac; this.name = name; this.version = version;
    }

    @Override public String toString() {
      return "Device{" + name + " mac=" + mac + " ip=" + ip.getHostAddress() + " fw=" + version + " enc=" + enc + "}";
    }
  }

  public enum Enc { ECB, GCM, UNKNOWN }

  // === Public API ===

  /** Broadcast discovery; returns devices (no deviceKey yet). */
  public List<Device> discover() throws IOException {
    String scan = new JSONObject().put("t", "scan").toString();
    System.out.println("GreeLanClient: Sending broadcast scan: " + scan);
    
    DatagramSocket sock = new DatagramSocket();
    sock.setSoTimeout(recvTimeoutMs);
    sock.setBroadcast(true);

    sendUdp(sock, BROADCAST_IP, GREE_UDP_PORT, scan);
    System.out.println("GreeLanClient: Broadcast sent to " + BROADCAST_IP + ":" + GREE_UDP_PORT);

    long end = System.currentTimeMillis() + recvTimeoutMs;
    List<Device> results = new ArrayList<>();
    int packetCount = 0;

    while (System.currentTimeMillis() < end) {
      try {
        JSONObject msg = recvJson(sock);
        packetCount++;
        System.out.println("GreeLanClient: Received packet #" + packetCount + ": " + msg.toString());
        
        if (!"pack".equals(msg.optString("t"))) {
          System.out.println("GreeLanClient: Ignoring non-pack message");
          continue;
        }

        String encPack = msg.optString("pack", null);
        if (encPack == null) {
          System.out.println("GreeLanClient: No pack field found");
          continue;
        }

        try {
          // Discovery replies are ECB with the GENERIC key
          String inner = decryptEcbBase64(encPack, GENERIC_KEY);
          System.out.println("GreeLanClient: Decrypted inner: " + inner);
          
          JSONObject pack = new JSONObject(inner); // {"t":"dev","mac":"...","name":"...","ver":"..."}
          if (!"dev".equals(pack.optString("t"))) {
            System.out.println("GreeLanClient: Ignoring non-dev response: " + pack.optString("t"));
            continue;
          }

          String mac = pack.optString("mac");
          String name = pack.optString("name", "<unknown>");
          String ver  = pack.optString("ver", "");
          InetAddress src = ((InetSocketAddress)((DatagramPacketHolder) msg.opt("_pkt")).packet.getSocketAddress()).getAddress();

          System.out.println("GreeLanClient: Found device - MAC: " + mac + ", Name: " + name + ", IP: " + src.getHostAddress());
          Device d = new Device(src, mac, name, ver);
          results.add(d);
          devicesByMac.put(mac, d);
        } catch (Exception e) {
          System.out.println("GreeLanClient: Error processing packet: " + e.getMessage());
        }
        
      } catch (SocketTimeoutException e) {
        System.out.println("GreeLanClient: Timeout reached, ending discovery");
        break;
      } catch (Exception ignoreBadPacket) {
        System.out.println("GreeLanClient: Bad packet ignored: " + ignoreBadPacket.getMessage());
      }
    }
    sock.close();
    System.out.println("GreeLanClient: Discovery complete. Found " + results.size() + " devices after processing " + packetCount + " packets");
    return results;
  }

  /** Bind to device to obtain its unique AES key (ECB using GENERIC key). More robust: unicast + broadcast + retries. */
  public String bind(Device d) throws IOException {
    IOException last = null;
    // Try a few times with increasing timeouts
    int[] timeouts = { Math.max(2000, recvTimeoutMs), Math.max(3000, recvTimeoutMs + 1000), Math.max(4500, recvTimeoutMs + 2500) };
    for (int t : timeouts) {
      try {
        return doBindOnce(d, t);
      } catch (SocketTimeoutException e) {
        last = e;
      }
    }
    throw (last != null ? last : new IOException("Bind failed (no reply)"));
  }

  private String doBindOnce(Device d, int timeoutMs) throws IOException {
    JSONObject inner = new JSONObject().put("mac", d.mac).put("t", "bind").put("uid", 0);
    String encInner = encryptEcbBase64(inner.toString(), GENERIC_KEY);
    JSONObject outer = wrapOuter(d.mac, encInner);
    String payload = outer.toString();

    DatagramSocket sock = new DatagramSocket();
    try {
      sock.setSoTimeout(timeoutMs);
      sock.setBroadcast(true);

      // Send unicast then broadcast; some firmwares only reply to broadcast.
      sendUdp(sock, d.ip.getHostAddress(), GREE_UDP_PORT, payload);
      sendUdp(sock, BROADCAST_IP,        GREE_UDP_PORT, payload);

      long deadline = System.currentTimeMillis() + timeoutMs;
      while (System.currentTimeMillis() < deadline) {
        JSONObject msg = recvJson(sock);
        if (!"pack".equals(msg.optString("t"))) continue;

        String innerResp = decryptEcbBase64(msg.getString("pack"), GENERIC_KEY);
        JSONObject pack = new JSONObject(innerResp);
        if ("bindok".equals(pack.optString("t")) && pack.optInt("r", 0) == 200) {
          String key = pack.getString("key");
          d.deviceKey = key;
          return key;
        }
        // ignore unrelated packets
      }
      throw new SocketTimeoutException("Receive timed out (bind)");
    } finally {
      sock.close();
    }
  }

  /** Auto-detect working encryption (ECB first, then GCM). Call after bind(). */
  public Enc negotiateEncryption(Device d) throws IOException {
    ensureDeviceKey(d);

    // Try ECB status
    try {
      JSONObject s = getStatus(d, Enc.ECB);
      if ("dat".equals(s.optString("t"))) {
        d.enc = Enc.ECB;
        return d.enc;
      }
    } catch (Exception ignored) {}

    // Try GCM status
    JSONObject s2 = getStatus(d, Enc.GCM);
    if ("dat".equals(s2.optString("t"))) {
      d.enc = Enc.GCM;
      return d.enc;
    }

    throw new IOException("Unable to negotiate encryption (ECB and GCM failed).");
  }

  /** Power ON/OFF (auto-enc if UNKNOWN). */
  public void setPower(Device d, boolean on) throws IOException {
    cmdAuto(d, new String[]{"Pow"}, new Object[]{on ? 1 : 0});
  }

  /** Set target temperature in Celsius (16..30 typical). */
  public void setTemperatureC(Device d, int celsius) throws IOException {
    cmdAuto(d, new String[]{"TemUn", "SetTem"}, new Object[]{0, celsius});
  }

  /** Get a small status payload (auto-enc if UNKNOWN). */
  public JSONObject getStatus(Device d) throws IOException {
    return getStatus(d, d.enc == Enc.UNKNOWN ? negotiateEncryption(d) : d.enc);
  }

  // === Internals ===

  private void cmdAuto(Device d, String[] opt, Object[] p) throws IOException {
    if (d.enc == Enc.UNKNOWN) negotiateEncryption(d);
    cmd(d, d.enc, opt, p);
  }

  private void cmd(Device d, Enc enc, String[] opt, Object[] p) throws IOException {
    ensureDeviceKey(d);
    JSONObject inner = new JSONObject()
        .put("opt", new JSONArray(Arrays.asList(opt)))
        .put("p", new JSONArray(Arrays.asList(p)))
        .put("t", "cmd");
    String encInner = encryptPack(inner.toString(), d.deviceKey, enc);

    JSONObject outer = wrapOuter(d.mac, encInner);
    JSONObject resp = sendAndRecv(d.ip, outer);
    String innerResp = decryptPack(resp.getString("pack"), d.deviceKey, enc);
    JSONObject pack = new JSONObject(innerResp);
    if (pack.optInt("r", 0) != 200)
      throw new IOException("Command failed: " + pack);
  }

  private JSONObject getStatus(Device d, Enc enc) throws IOException {
    ensureDeviceKey(d);
    JSONObject inner = new JSONObject()
        .put("cols", new JSONArray(Arrays.asList("Pow","Mod","SetTem","WdSpd","TemUn","TemRec","Lig","Tur","Quiet")))
        .put("mac", d.mac)
        .put("t", "status");
    String encInner = encryptPack(inner.toString(), d.deviceKey, enc);

    JSONObject outer = wrapOuter(d.mac, encInner);
    JSONObject resp = sendAndRecv(d.ip, outer);
    String innerResp = decryptPack(resp.getString("pack"), d.deviceKey, enc);
    return new JSONObject(innerResp); // {"t":"dat","cols":[...],"dat":[...]}
  }

  /** Hardened send: unicast + broadcast; small retry cycle. */
  private JSONObject sendAndRecv(InetAddress ip, JSONObject json) throws IOException {
    String payload = json.toString();
    int[] timeouts = { recvTimeoutMs, Math.max(1500, recvTimeoutMs) };

    for (int t : timeouts) {
      DatagramSocket sock = new DatagramSocket();
      try {
        sock.setSoTimeout(t);
        sock.setBroadcast(true);

        // Unicast first, then broadcast (tcid in payload routes to correct device)
        sendUdp(sock, ip.getHostAddress(), GREE_UDP_PORT, payload);
        sendUdp(sock, BROADCAST_IP,        GREE_UDP_PORT, payload);

        long deadline = System.currentTimeMillis() + t;
        while (System.currentTimeMillis() < deadline) {
          JSONObject msg = recvJson(sock);
          if ("pack".equals(msg.optString("t"))) {
            return msg;
          }
        }
      } catch (SocketTimeoutException ignore) {
        // try next timeout
      } finally {
        sock.close();
      }
    }
    throw new SocketTimeoutException("Receive timed out");
  }

  /** Instance method so we can use a sequential request id. */
  private JSONObject wrapOuter(String mac, String packBase64) {
    return new JSONObject()
        .put("cid", "app")
        .put("i", seq.getAndIncrement()) // sequential id
        .put("pack", packBase64)
        .put("t", "pack")
        .put("tcid", mac)
        .put("uid", 0);
  }

  private static void sendUdp(DatagramSocket sock, String host, int port, String payload) throws IOException {
    byte[] data = payload.getBytes(StandardCharsets.UTF_8);
    DatagramPacket p = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
    sock.send(p);
  }

  private JSONObject recvJson(DatagramSocket sock) throws IOException {
    byte[] buf = new byte[4096];
    DatagramPacket packet = new DatagramPacket(buf, buf.length);
    sock.receive(packet);
    String s = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
    JSONObject j = new JSONObject(s);
    j.put("_pkt", new DatagramPacketHolder(packet));
    return j;
  }

  // === Encryption helpers ===

  private static String encryptPack(String plaintext, String key16, Enc enc) {
    switch (enc) {
      case ECB: return encryptEcbBase64(plaintext, key16);
      case GCM: return encryptGcmBase64(plaintext, key16);
      default:  throw new IllegalStateException("Unknown encryption mode");
    }
  }

  private static String decryptPack(String base64Pack, String key16, Enc enc) {
    switch (enc) {
      case ECB: return decryptEcbBase64(base64Pack, key16);
      case GCM: return decryptGcmBase64(base64Pack, key16);
      default:  throw new IllegalStateException("Unknown encryption mode");
    }
  }

  // AES-128-ECB + PKCS5/7 padding + Base64
  private static String encryptEcbBase64(String plaintext, String key16) {
    try {
      Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
      c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key16.getBytes(StandardCharsets.UTF_8), "AES"));
      byte[] out = c.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(out);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String decryptEcbBase64(String base64, String key16) {
    try {
      byte[] enc = Base64.getDecoder().decode(base64);
      Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
      c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key16.getBytes(StandardCharsets.UTF_8), "AES"));
      byte[] out = c.doFinal(enc);
      return new String(out, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // AES-128-GCM + Base64
  // Framing: Base64( IV(12) || CIPHERTEXT||TAG(16) )
  private static String encryptGcmBase64(String plaintext, String key16) {
    try {
      byte[] iv = new byte[GCM_IV_LEN];
      new SecureRandom().nextBytes(iv);
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key16.getBytes(StandardCharsets.UTF_8), "AES"),
          new GCMParameterSpec(GCM_TAG_BITS, iv));
      byte[] ctAndTag = c.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      byte[] out = new byte[iv.length + ctAndTag.length];
      System.arraycopy(iv, 0, out, 0, iv.length);
      System.arraycopy(ctAndTag, 0, out, iv.length, ctAndTag.length);
      return Base64.getEncoder().encodeToString(out);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String decryptGcmBase64(String base64, String key16) {
    try {
      byte[] all = Base64.getDecoder().decode(base64);
      if (all.length < GCM_IV_LEN + 16) throw new IllegalArgumentException("GCM pack too short");
      byte[] iv = Arrays.copyOfRange(all, 0, GCM_IV_LEN);
      byte[] ctTag = Arrays.copyOfRange(all, GCM_IV_LEN, all.length);

      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key16.getBytes(StandardCharsets.UTF_8), "AES"),
          new GCMParameterSpec(GCM_TAG_BITS, iv));
      byte[] pt = c.doFinal(ctTag);
      return new String(pt, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void ensureDeviceKey(Device d) {
    if (d.deviceKey == null || d.deviceKey.length() != 16)
      throw new IllegalStateException("Device not bound (missing 16-byte AES key). Call bind() first.");
  }

  /** Hold the original DatagramPacket (to read source IP during discovery). */
  private static class DatagramPacketHolder {
    final DatagramPacket packet;
    DatagramPacketHolder(DatagramPacket packet) { this.packet = packet; }
  }
}