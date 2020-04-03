package com.witcher;

import it.unisa.dia.gas.jpbc.*;
import function.*;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args){
        /* test SM4*/
        byte[] str = {0x1, 0x2, 0x3};
        byte[] key = new byte[16];
        Arrays.fill(key, (byte)12);
        byte[] cipher = CryptographicAlgorithm.SM4_Encrypt_ECB(key, str);
        System.out.println(new String(Hex.encode(cipher)));
        byte[] str_ = CryptographicAlgorithm.SM4_Decrypt_ECB(key, cipher);
        System.out.println(new String(Hex.encode(str_)));
//        System.out.println("\n" + SM3.byteArrayToHexString(SM3.hash(test.getBytes())));
//        System.out.println("\nhash end");
//        SM3Digest sm3 = new SM3Digest();
//        byte[] msg1 = test.getBytes(), md = new byte[32];
//        sm3.update(msg1, 0, msg1.length);
//        sm3.doFinal(md, 0);
//        String s = new String(Hex.encode(md));
//        System.out.println(s.toUpperCase());
        //System.out.println(CryptographicAlgorithm.SM3("abc".getBytes()));
        //CryptoTools.test();
    }
}
