package org.seedstack.seed.web.security.internal;

import sun.misc.BASE64Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SignatureException;

final class Signature {

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    /**
     * Computes an HMAC signature (based on RFC 2104-compliant).
     *
     * @param key  The signing key
     * @param data The data to be signed
     * @return The Base64-encoded HMAC signature.
     * @throws java.security.SignatureException when signature generation fails
     */
    static String hmac(String key, String data) throws SignatureException {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA256_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            BASE64Encoder encoder = new BASE64Encoder();
            return encoder.encode(rawHmac);
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
    }
}