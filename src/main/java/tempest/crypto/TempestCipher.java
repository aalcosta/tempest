package tempest.crypto;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class TempestCipher {

    private Cipher cipher;

    private Cipher decipher;
    
    public TempestCipher(KeyPair keyPair, String keyAlg) {
        try {
            cipher = Cipher.getInstance(keyAlg);
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());

            decipher = Cipher.getInstance(keyAlg);
            decipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public TempestCipher(SecretKey key, String keyAlg) {
        try {
            cipher = Cipher.getInstance(keyAlg);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            decipher = Cipher.getInstance(keyAlg);
            decipher.init(Cipher.DECRYPT_MODE, key);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized String cipher(String in) {
        try {
            byte[] value = cipher.doFinal(in.getBytes());
            return Base64.encodeBase64String(value);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized String decipher(String in) {
        try {
            if (in == null) {
                return null;
            }

            byte[] value = Base64.decodeBase64(in);
            return new String(decipher.doFinal(value));
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

}
