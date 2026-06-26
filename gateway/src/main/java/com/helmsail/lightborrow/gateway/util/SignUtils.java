package com.helmsail.lightborrow.gateway.util;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.gateway.exception.GatewayException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * IM 平台签名工具类。
 */
public class SignUtils {

    private SignUtils() {}

    /**
     * HMAC-SHA256 签名（飞书、钉钉通用）。
     *
     * @param secret    密钥
     * @param timestamp 时间戳（秒级或毫秒级字符串）
     * @return Base64 编码的签名值
     */
    public static String hmacSha256(String secret, String timestamp) {
        try {
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signData);
        } catch (GeneralSecurityException e) {
            throw new GatewayException(ErrorCode.GATEWAY_CHANNEL_ERROR, e, "HMAC-SHA256 签名计算失败");
        }
    }

    /**
     * SHA-1 签名（企业微信回调验证用）。
     *
     * @param tokens 待拼接的字符串数组
     * @return 小写十六进制 SHA-1 值
     */
    public static String sha1(String... tokens) {
        try {
            StringBuilder sb = new StringBuilder();
            for (String token : tokens) {
                sb.append(token);
            }
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new GatewayException(ErrorCode.GATEWAY_CHANNEL_ERROR, e, "SHA-1 签名计算失败");
        }
    }
}
