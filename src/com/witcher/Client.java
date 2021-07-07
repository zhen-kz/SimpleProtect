package com.witcher;

import function.ClientCryptoTools;
import function.CryptoTools;
import function.CryptographicAlgorithm;
import it.unisa.dia.gas.jpbc.Element;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Client {
    private ClientCryptoTools tools;
    private Element r;
    private String password;
    private Element psw;

    public Client() {
        tools = new ClientCryptoTools("a.properties", "P.properties");
    }

    private static byte[] Int2Bytes(int num) {
        byte[] res = new byte[4];
        byte t;
        for (byte i : res) {
            i = (byte) (num & 0xff);
            num >>>= 8;
        }
        return res;
    }

    public Element ConfusePassword(@NotNull String password) {
        this.password = password;
        r = tools.getRandomElementInZpPlus().getImmutable();
        psw = tools.H(password.getBytes()).powZn(r).getImmutable();
        return psw.getImmutable();
    }

    public boolean CheckSigmaI(Element[] sigma_i, Element[] PKi) {
        if (sigma_i == null) return false;
        boolean res = true, temp;
        for (int i = 0; i < sigma_i.length; ++i) {
            System.out.printf("Client: checking IS%d's σi  ", i + 1);
            System.out.println(temp = tools.CheckE(sigma_i[i], tools.getGGeneratorP(), psw, PKi[i]));
            res &= temp;
        }
        return res;
    }

    // t sigma_i
    public Element ComputeSigmaU(Element[] sigma_i, int[] index) {
        return tools.ComputeSigmaU(sigma_i, index, r);
    }

    public boolean CheckSigmaU(Element sigma_u, Element PK) {
        boolean status = tools.CheckE(sigma_u, tools.getGGeneratorP(), tools.H(password.getBytes()), PK);
        System.out.println("user: checking σu:  " + status);
        return status;
    }

    public byte[] ComputeSpu(Element sigma_u) {
        if (sigma_u == null) return null;
        return CryptographicAlgorithm.PRF(CryptographicAlgorithm.SM3(sigma_u.toBytes()),
                password.getBytes(), 128);
    }

    public byte[][] ComputeSpi(byte[] sp_u, int[] index) {
        if (sp_u == null || index == null || index.length <= 0) return null;
        byte[][] sp_i = new byte[index.length][];
        byte[] buffer = new byte[sp_u.length + 4];
        for (int i = 0; i < index.length; ++i) {
            System.arraycopy(sp_u, 0, buffer, 0, sp_u.length);
            System.arraycopy(Int2Bytes(index[i]), 0, buffer, sp_u.length, 4);
            //产生128位哈希值作为SM3的密钥
            sp_i[i] = CryptographicAlgorithm.MD5(buffer);
        }
        return sp_i;
    }

    public byte[][] EncryptRi(byte[][] sp_i, Element[] r_i) {
        if (sp_i == null || r_i == null || sp_i.length != r_i.length) return null;
        byte[][] res = new byte[r_i.length][];
        for (int i = 0; i < r_i.length; ++i) {
            res[i] = CryptographicAlgorithm.SM4_Encrypt_ECB(sp_i[i], r_i[i].toBytes());
        }
        return res;
    }

    public HashMap<String, byte[][]> DecryptToken(byte[][] sp_i, byte[][] cipher, int bytesLen) {
        if (cipher == null) return null;
        byte[][] aut_i = new byte[cipher.length][], token = new byte[cipher.length][];
        byte[] msg;
        final int aut_len = 128;
        for (int i = 0; i < cipher.length; ++i) {
            msg = CryptographicAlgorithm.SM4_Decrypt_ECB(sp_i[i], cipher[i], bytesLen);
            aut_i[i] = new byte[aut_len];
            System.arraycopy(msg, 0, aut_i[i], 0, aut_len);
            token[i] = new byte[msg.length - aut_len];
            System.arraycopy(msg, aut_len, token[i], 0, msg.length - aut_len);
        }
        HashMap<String, byte[][]> res = new HashMap<>();
        res.put("aut_i", aut_i);
        res.put("token", token);
        return res;
    }

    public Element[] Aut2Element(byte[][] aut) {
        if (aut == null) return null;
        Element[] res = new Element[aut.length];
        for (int i = 0; i < aut.length; ++i) {
            res[i] = tools.getElementFromBytesInG(aut[i]);
        }
        return res;
    }

    public boolean CheckAutToken(Element[] aut_i, byte[] token, Element[] PKi) {
        if (aut_i == null || token == null) return false;
        boolean res = true, temp;
        for (int i = 0; i < aut_i.length; ++i) {
            temp = tools.CheckE(aut_i[i], tools.getGGeneratorP(),
                    tools.H(token), PKi[i]);
            System.out.println("user: Checking Aut" + (i + 1) + ":  " + temp);
            res &= temp;
        }
        return res;
    }

    public Element ComputeAutU(Element[] aut_i, int[] index){
        return tools.ComputeAutU(aut_i, index);
    }

    public boolean CheckAutU(Element aut_u, byte[] token, Element PK){
        if (aut_u == null || token == null || PK == null) return false;
        return tools.CheckE(aut_u, tools.getGGeneratorP(),
                tools.H(token), PK);
    }

    public boolean test() {
        Element psw = ConfusePassword("abc");
        Element r = tools.getRandomElementInZpPlus();
        System.out.println(tools.CheckE(psw.duplicate().powZn(r), tools.getGGeneratorP(),
                psw, tools.getGGeneratorP().powZn(r)));
        return true;
    }
}
