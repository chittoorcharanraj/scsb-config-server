package org.recap.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class SecurityUtil {

    @Value("${scsb.encryption.secretkey}")
    private String encryptionSecretKey;  // Expects key length to be 16 exactly

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    public String getDecryptedValue(String encryptedValue) {
        Key aesKey = new SecretKeySpec(encryptionSecretKey.getBytes(), "AES");
        Base64.Decoder decoder = Base64.getDecoder();
        Cipher cipher = null;
        String decrypted = null;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            decrypted = new String(cipher.doFinal(decoder.decode(encryptedValue)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
            logger.error("error--> {}", e);
        }
        return decrypted;
    }
}
