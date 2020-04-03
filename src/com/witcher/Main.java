package com.witcher;

import it.unisa.dia.gas.jpbc.*;
import function.*;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.encoders.Hex;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public class Main {
    private IdentifyServer[] IS;

    public IdentifyServer[] getISList() {
        return IS;
    }

    public static void main(String[] args) throws Exception {
//        ClientCryptoTools client = new ClientCryptoTools("a.properties", "P.properties");
//        IdentifyServerCryptoTools is = new IdentifyServerCryptoTools("a.properties", "P.properties");
//        CryptoTools.test();
        final int t = 2, n = 3;
        Client user = new Client();
        IdentifyServer IS = new IdentifyServer(t, n);
//        if (user.test())
//            return;
        /*variable*/

        boolean status;
        String password = "hello world";
        Element psw, sigma_u;
        HashMap<String, Element[]> PKs;
        Element[] sigma_i;
        byte[] sp_u;
        byte[][] sp_i;



        /*-------------register-------------*/


        //客户端混淆密码
        psw = user.ConfusePassword(password);
        //IS产生ki, PKi, PK
        PKs = IS.MasterKeyGen();
        Element[] PKi = PKs.get("PKi");
        Element PK = PKs.get("PK")[0];
        //IS计算σi
        sigma_i = IS.ComputeSigma_i(psw);
        //客户端检查σi
        user.CheckSigmaI(sigma_i, PKi);
        //客户端计算σu
        Element[] sigma_i_buffer = new Element[t];
        System.arraycopy(sigma_i, 0, sigma_i_buffer, 0, t);
        sigma_u = user.ComputeSigmaU(sigma_i_buffer, new int[]{1, 2});
        //检查σu
        user.CheckSigmaU(sigma_u, PK);
        //计算spu, spi
        sp_u = user.ComputeSpu(sigma_u);
        System.out.println("sp_u: " + new String(Hex.encode(sp_u)));
        sp_i = user.ComputeSpi(sp_u, new int[]{1, 2, 3});
        //IS保存spi
        IS.SetSpi(sp_i);



        /*-------------sign-on-------------*/


        /*RetriSerPsw*/
        //客户端应在这个阶段获取PKi和PK, 但为了方便继续使用register阶段的变量

        //客户端混淆密码
        psw = user.ConfusePassword("hello world");
        //IS检查ρ
        if (!IS.CheckPhiRhoAndIncreaseRho()) {
            System.out.println("ρ > MAX_ρ");
        }
        //计算{σi, ri}
        HashMap<String, Element[]> sigma_i_Ri = IS.ComputeSigma_iAndRi(psw);
        sigma_i = sigma_i_Ri.get("sigma_i");
        Element[] r_i = sigma_i_Ri.get("r_i");
        //客户端检查σi是否正确
        user.CheckSigmaI(sigma_i, PKi);
        //计算σu
        System.arraycopy(sigma_i, 0, sigma_i_buffer, 0, t);
        sigma_u = user.ComputeSigmaU(sigma_i_buffer, new int[]{1, 2});
        //检查σu是否正确
        user.CheckSigmaU(sigma_u, PK);


        //Authentication

        //客户端计算spu, spi
        sp_u = user.ComputeSpu(sigma_u);
        sp_i = user.ComputeSpi(sp_u, new int[]{1, 2, 3});
        //客户端用spi加密IS的随机数ri
        byte[][] Esp_i = user.EncryptRi(sp_i, r_i);
        //IS解密
        Element[] r = IS.DecryptRi(Esp_i, r_i[0].getLengthInBytes());
        //检查随机数是否正确
        IS.CheckRi(r);
        //IS计算aut_i
        final String Token = "uestc";
        Element[] aut_i = IS.ComputeAUTi(Token);
        //将{aut_i||Token}用sp_i加密
        byte[][] EAutToken_i = IS.EncryptToken(aut_i, Token);
        //客户端解密
        final int bytes_len = aut_i[0].getLengthInBytes() + Token.getBytes().length;
        HashMap<String, byte[][]> de = user.DecryptToken(sp_i, EAutToken_i, bytes_len);
        aut_i = user.Aut2Element(de.get("aut_i"));
        System.out.println("Token bytes:       " + new String(Hex.encode(Token.getBytes())));
        System.out.println("user_Token bytes:  " + new String(Hex.encode(de.get("token")[0])));
        //检查aut_i与Token是否正确
        user.CheckAutToken(aut_i, de.get("token")[0], PKi);
        //计算Aut_u
        Element aut_u = user.ComputeAutU(aut_i, new int[]{1, 2, 3});
        String user_Token = new String(de.get("token")[0]);
        System.out.println("user_Token:  " + user_Token);
        //检查Aut_u是否正确
        status = user.CheckAutU(aut_u, user_Token.getBytes(), PK);
        System.out.println("Aut_u checking:  " + status);


        /*-------------RenewShare-------------*/


        PKi = IS.RenewShare();
        sigma_i = IS.ComputeSigma_i(psw);
        user.CheckSigmaI(sigma_i, PKi);
        sigma_u = user.ComputeSigmaU(sigma_i, new int[]{1,2,3});
        user.CheckSigmaU(sigma_u, PK);
        for (int i = 0; i < PKi.length; ++i) {
            System.out.printf("new PK%d:  " + PKi[i] + "\n", i + 1);
        }
    }
}
