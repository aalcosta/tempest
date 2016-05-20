package br.com.tempest.crypto;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.commons.codec.binary.Base64;

import br.com.tempest.auth.TempestIOUtils;

public class TempestSigner {

    private KeyPair keyPair;
    private String signAlg;

    public TempestSigner(String keyAlg, String signAlg) throws NoSuchAlgorithmException {
        this(KeyPairGenerator.getInstance(keyAlg).generateKeyPair(), signAlg);
    }

    public TempestSigner(KeyPair keyPair, String signAlg) {
        this.keyPair = keyPair;
        this.signAlg = signAlg;
    }

    public synchronized String sign(Serializable data) {
        try {
            byte[] sData = TempestIOUtils.serialize(data);
            return sign(sData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized String sign(byte[] data) {
        try {
            Signature sig = Signature.getInstance(signAlg);
            sig.initSign(keyPair.getPrivate());
            sig.update(data);
            byte[] value = sig.sign();
            return Base64.encodeBase64String(value);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized boolean verify(String in, Serializable data) {
        try {
            byte[] sData = TempestIOUtils.serialize(data);
            return verify(in, sData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized boolean verify(String in, byte[] data) {
        try {
            if (in == null || data == null) {
                return false;
            }
            byte[] value = Base64.decodeBase64(in);

            Signature verifier = Signature.getInstance(signAlg);
            verifier.initVerify(keyPair.getPublic());
            verifier.update(data);
            return verifier.verify(value);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }

}
