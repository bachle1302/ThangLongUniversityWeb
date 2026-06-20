package com.example.ThangLongUniversityWeb.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@Configuration
public class VNPayConfig {

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret:${vnpay.secret-key:}}")
    private String secretKey;

    @Value("${vnpay.payment-url:${vnpay.pay-url:}}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.version}")
    private String vnp_Version;

    @Value("${vnpay.command}")
    private String vnp_Command;

    public String getVnpTmnCode() { return vnp_TmnCode; }
    public String getSecretKey()   { return secretKey; }
    public String getVnpPayUrl()   { return vnp_PayUrl; }
    public String getVnpReturnUrl(){ return vnp_ReturnUrl; }
    public String getVnpVersion()  { return vnp_Version; }
    public String getVnpCommand()  { return vnp_Command; }

    // Hàm 1: Băm dữ liệu (Mã hóa bảo mật)
    public String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    // Hàm 2: Lấy địa chỉ IP của sinh viên đang bấm thanh toán
    public String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP";
        }
        return ipAdress;
    }

    // Hàm 3: Tạo mã giao dịch ngẫu nhiên (SecureRandom — an toàn cho giao dịch tài chính)
    public String getRandomNumber(int len) {
        SecureRandom rnd = new SecureRandom();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
