package tempest.encoding;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utilities for handling Encoding/Decoding of Strings, charset and base changes
 * <p>
 * Created by alexandre on 12/14/16.
 */
public class EncodingUtils {

    public static Charset DEFAULT_CHARSET = Charset.forName(
            System.getProperty("project.build.sourceEncoding", UTF_8.name()));

    public static String asString(byte[] strBytes) {
        return asString(strBytes, DEFAULT_CHARSET);
    }

    public static String asString(byte[] strBytes, String charset) throws UnsupportedCharsetException {
        return asString(strBytes, Charset.forName(charset));
    }

    public static String asString(byte[] strBytes, Charset charset) {
        return new String(strBytes, charset);
    }

    public static String changeCharset(String src, Charset charset) {
        return new String(getBytes(src), charset);
    }

    public static String changeCharset(String src, Charset srcCharset, Charset charset) {
        return new String(getBytes(src, srcCharset), charset);
    }

    public static byte[] decodeBase64(String src) {
        return Base64.decodeBase64(src);
    }

    public static String decodeBase64ToString(String src) {
        return asString(decodeBase64(src));
    }


    public static String encodeBase64(String src) {
        return encodeBase64(src, false);
    }

    public static String encodeBase64(String src, boolean urlSafe) {
        return encodeBase64(getBytes(src), urlSafe);
    }

    public static String encodeBase64(byte[] src) {
        return encodeBase64(src, false);
    }

    public static String encodeBase64(byte[] src, boolean urlSafe) {
        return urlSafe ? Base64.encodeBase64String(src) : Base64.encodeBase64URLSafeString(src);
    }

    public static byte[] getBytes(String src) {
        return getBytes(src, DEFAULT_CHARSET);
    }

    public static byte[] getBytes(String src, String charset) throws UnsupportedCharsetException {
        return getBytes(src, Charset.forName(charset));
    }

    public static byte[] getBytes(String src, Charset charset) {
        return src.getBytes(charset);
    }

    public static byte[] serialize(Serializable serializable) throws IOException {
        ByteArrayOutputStream bos = null;
        ObjectOutput out = null;

        try {
            bos = new ByteArrayOutputStream();

            out = new ObjectOutputStream(bos);
            out.writeObject(serializable);
            out.flush();

            return bos.toByteArray();
        } finally {
            try {
                if (out != null) out.close();
                if (bos != null) bos.close();
            } catch (IOException ex) {
                // Do nothing
            }
        }
    }

    public static Object deserialize(byte[] serialized) throws IOException, ClassNotFoundException {

        ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
        ObjectInput oin = null;
        try {
            oin = new ObjectInputStream(bis);
            return oin.readObject();
        } finally {
            try {
                if (oin != null) oin.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

}
