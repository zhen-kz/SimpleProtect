package function;

import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.jetbrains.annotations.NotNull;
import org.bouncycastle.crypto.digests.SM3Digest;

import java.util.Arrays;

public class CryptographicAlgorithm {
    public static byte[] SM3(byte[] message) {
        if (message == null) return null;
        SM3Digest sm3 = new SM3Digest();
        sm3.update(message, 0, message.length);
        byte[] res = new byte[32];
        sm3.doFinal(res, 0);
        return res;
    }

    public static byte[] SHA1(byte[] message) {
        if (message == null) return null;
        SHA1Digest sha1 = new SHA1Digest();
        sha1.update(message, 0, message.length);
        byte[] res = new byte[20];
        sha1.doFinal(res, 0);
        return res;
    }

    public static byte[] SHA256(byte[] message) {
        if (message == null) return null;
        SHA256Digest sha256 = new SHA256Digest();
        sha256.update(message, 0, message.length);
        byte[] res = new byte[32];
        sha256.doFinal(res, 0);
        return res;
    }

    public static byte[] MD5(byte[] message) {
        if (message == null) return null;
        MD5Digest MD5 = new MD5Digest();
        MD5.update(message, 0, message.length);
        byte[] res = new byte[16];
        MD5.doFinal(res, 0);
        return res;
    }

    public static byte[] HmacSHA1(byte[] secret, byte[] seed) {
        if (secret == null || seed == null) return null;
        KeyParameter keyParameter = new KeyParameter(secret);
        SHA1Digest digest = new SHA1Digest();
        HMac hmac = new HMac(digest);
        hmac.init(keyParameter);
        hmac.update(seed, 0, seed.length);
        byte[] res = new byte[hmac.getMacSize()];
        hmac.doFinal(res, 0);
        return res;
    }

    public static byte[] HmacSM3(byte[] secret, byte[] seed) {
        if (secret == null || seed == null) return null;
        KeyParameter keyParameter = new KeyParameter(secret);
        SM3Digest digest = new SM3Digest();
        HMac hmac = new HMac(digest);
        hmac.init(keyParameter);
        hmac.update(seed, 0, seed.length);
        byte[] res = new byte[hmac.getMacSize()];
        hmac.doFinal(res, 0);
        return res;
    }

    public static byte[] P_SM3(byte[] secret, byte[] seed, int bitLen) {
        if (secret == null || seed == null || bitLen <= 0) return null;
        final int SM3_BYTE_LENGTH = 32;
        int byteLen = bitLen / 8, pos = 0;
        byte[] res = new byte[byteLen], res_temp;
        byte[] A = HmacSM3(secret, seed);
        byte[] A_plus_seed = new byte[32 + seed.length];
        System.arraycopy(seed, 0, A_plus_seed, SM3_BYTE_LENGTH, seed.length);
        while (pos < byteLen) {
            System.arraycopy(A, 0, A_plus_seed, 0, A.length);
            res_temp = HmacSM3(secret, A_plus_seed);
            if (byteLen - pos < SM3_BYTE_LENGTH) {
                System.arraycopy(res_temp, 0, res, pos, byteLen - pos);
                pos += SM3_BYTE_LENGTH;
                A = HmacSM3(secret, A);
            } else {
                System.arraycopy(res_temp, 0, res, pos, SM3_BYTE_LENGTH);
                pos = byteLen;
            }
        }
        return res;
    }

    //pseudo random function
    public static byte[] PRF(byte[] secret, byte[] seed, int bitLen) {
        return P_SM3(secret, seed, bitLen);
    }

    //SM4 ECB
    public static byte[] SM4_Encrypt_ECB(byte[] key, byte[] data) {
        if (key == null || key.length != 16 || data == null) return null;
        SM4Engine engine = new SM4Engine();
        KeyParameter keyParameter = new KeyParameter(key);
        engine.init(true, keyParameter);
        int resLen, r;
        if ((r = data.length % engine.getBlockSize()) == 0) {
            resLen = data.length;
        } else {
            resLen = data.length + engine.getBlockSize() - r;
        }
        byte[] block = new byte[engine.getBlockSize()], res = new byte[resLen];
        int pos = 0;
        while (pos < data.length) {
            Arrays.fill(block, (byte) 0);
            System.arraycopy(data, pos, block, 0, Math.min(data.length - pos, block.length));
            engine.processBlock(block, 0, res, pos);
            pos += engine.getBlockSize();
        }
        return res;
    }

    public static byte[] SM4_Decrypt_ECB(byte[] key, byte[] cipher, int bytesLen) {
        if (key == null || cipher == null || key.length != 16) return null;
        SM4Engine engine = new SM4Engine();
        if (cipher.length % engine.getBlockSize() != 0) return null;
        byte[] res_temp = new byte[cipher.length];
        int pos = 0;
        KeyParameter keyParameter = new KeyParameter(key);
        engine.init(false, keyParameter);
        byte[] block = new byte[engine.getBlockSize()];
        while (pos < cipher.length) {
            System.arraycopy(cipher, pos, block, 0, block.length);
            engine.processBlock(block, 0, res_temp, pos);
            pos += engine.getBlockSize();
        }
        byte[] res;
        if (bytesLen > 0) {
            res = new byte[bytesLen];
            System.arraycopy(res_temp, 0, res, 0, bytesLen);
        } else {
            res = res_temp;
        }
        return res;
    }
}
