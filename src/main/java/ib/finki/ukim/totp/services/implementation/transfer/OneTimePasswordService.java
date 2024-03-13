package ib.finki.ukim.totp.services.implementation.transfer;

import ib.finki.ukim.totp.services.interfaces.transfer.IOneTimePasswordService;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class OneTimePasswordService implements IOneTimePasswordService {
    private static final int[] DIGITS_POWER = {1,10,100,1000,10000,100000,1000000,10000000,100000000 };

    private byte[] hmac_sha(String crypto, byte[] keyBytes, byte[] text){
        try {
            Mac hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");

            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    private byte[] hexStr2Bytes(String hex){
        byte[] bArray = new BigInteger("10" + hex,16).toByteArray();

        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    @Override
    public String generateTOTP(String key, LocalDateTime currentTime){
        long T0 = 0;
        long X = 30;
        long longCurrentTime = currentTime.toEpochSecond(ZoneOffset.UTC);

        long time = (longCurrentTime - T0)/X;
        StringBuilder steps = new StringBuilder(Long.toHexString(time).toUpperCase());

        while (steps.length() < 16) {
            steps.insert(0, "0");
        }

        return mathGenerateTOTP(key, steps.toString(), "8", "HmacSHA1");
    }

    public String mathGenerateTOTP(String key, String time, String returnDigits, String crypto){
        int codeDigits = Integer.decode(returnDigits);
        StringBuilder timeBuilder = new StringBuilder(time);

        while (timeBuilder.length() < 16 ) {
            timeBuilder.insert(0, "0");
        }

        time = timeBuilder.toString();

        byte[] msg = hexStr2Bytes(time);
        byte[] k = hexStr2Bytes(key);

        byte[] hash = hmac_sha(crypto, k, msg);

        //00001111
        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                    ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        StringBuilder result = new StringBuilder(Integer.toString(otp));

        while (result.length() < codeDigits) {
            result.insert(0, "0");
        }

        return result.toString();
    }

    public boolean verifyTOTP(String secretKey, String password, LocalDateTime timestamp) {
        String generatedPassword = generateTOTP(secretKey, timestamp);

        return generatedPassword.equals(password);
    }
}
