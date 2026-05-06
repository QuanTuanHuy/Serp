package serp.project.payment_service.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class để tạo HMAC SHA256 cho ZaloPay
 */
@Slf4j
public class ZaloPayHMACUtil {
    
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    /**
     * Tạo HMAC SHA256
     * 
     * @param data Dữ liệu cần mã hóa
     * @param key Key bí mật
     * @return HMAC string (hex format)
     */
    public static String computeHmacSHA256(String data, String key) {
        try {
            Mac hmac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            hmac.init(secretKeySpec);
            
            byte[] hmacBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            return bytesToHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error computing HMAC SHA256: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to compute HMAC SHA256", e);
        }
    }
    
    /**
     * Chuyển đổi byte array sang hex string
     * 
     * @param bytes Byte array
     * @return Hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * Verify HMAC
     * 
     * @param data Dữ liệu cần verify
     * @param key Key bí mật
     * @param receivedMac MAC nhận được
     * @return true nếu MAC hợp lệ
     */
    public static boolean verifyHmacSHA256(String data, String key, String receivedMac) {
        String computedMac = computeHmacSHA256(data, key);
        return computedMac.equalsIgnoreCase(receivedMac);
    }
}
