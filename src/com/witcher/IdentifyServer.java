package com.witcher;

import function.ClientCryptoTools;
import function.CryptographicAlgorithm;
import function.IdentifyServerCryptoTools;
import function.MysqlManager;
import it.unisa.dia.gas.jpbc.Element;

import java.util.HashMap;
import java.util.Map;


public class IdentifyServer {

    private IdentifyServerCryptoTools tools;
    private int t = 2;
    private int n = 3;
    private Element[] ki;
    private final int MAX_PHI = 5;
    private final int MAX_RHO = 5;
    //Φ
    private int phi;
    //ρ
    private int rho;
    private Element[] r_i;
    private byte[][] sp_i;

    public IdentifyServer() {
        tools = new IdentifyServerCryptoTools("a.properties", "P.properties");
    }

    public IdentifyServer(int t, int n) {
        tools = new IdentifyServerCryptoTools("a.properties", "P.properties");
        this.t = t;
        this.n = n;
    }

    private Element[] PKi;

    public HashMap<String, Element[]> MasterKeyGen()  {
        Element[][] fx = new Element[n][];
        Element[][] aP = new Element[n][];
        Element[][] fji = new Element[n][n];
        for (int i = 0; i < n; ++i) {
            fx[i] = tools.polynomialFx(t - 1);
            aP[i] = tools.Compute_aPs(fx[i]);
        }
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
//                if (i == j) continue;
                fji[i][j] = tools.ComputeFx(fx[j], i + 1);
            }
        }
        //check
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i == j) continue;
                System.out.printf("IS%d: verifying IS%d ...  :", i + 1, j + 1);
                System.out.println(tools.CheckFji_aP(fji[i][j], aP[j], i + 1));
            }
        }
        Element[] ki = new Element[n];
        Element[] PKi = new Element[n], a0P = new Element[n];
        for (int i = 0; i < n; ++i) {
            ki[i] = tools.ComputeKi(fji[i]);
            PKi[i] = tools.ComputePKi(ki[i]);
            a0P[i] = aP[i][0];
        }
        /*-------test-------*/
        ClientCryptoTools ctools = new ClientCryptoTools("a.properties", "P.properties");
        Element psw = ctools.H("hello world".getBytes()).powZn(ctools.getRandomElementInZpPlus()).getImmutable();
        for (int i = 0; i < n; ++i) {
            System.out.printf("(k%d, PK%d): ", i, i);
            System.out.println(ctools.CheckE(psw.powZn(ki[i]), tools.getGGeneratorP(),
                    psw, PKi[i]));
            System.out.println("ki: " + ki[i]);
            System.out.println("PKi: " + PKi[i]);
        }
        /*-------test end-------*/
        Element[] PK = new Element[1];
        PK[0] = tools.ComputePK(a0P);
        HashMap<String, Element[]> map = new HashMap<String, Element[]>();
        this.ki = ki;
        this.PKi = PKi;
        map.put("PKi", PKi);
        map.put("PK", PK);
        //数据库导入
        MysqlManager test=new MysqlManager();
        test.getConnection();
        String name="test_one";
        byte[] one=PKi[0].toString().getBytes();
        byte[] two=ki[0].toString().getBytes();
        test.Insert(name,one,two);
        System.out.println("在数据库中查找：");
        System.out.println(test.Select(name));
        test.close();
         return map;
    }

    //σ
    public Element[] ComputeSigma_i(Element psw) {
        if (ki == null || psw == null) return null;
        Element[] sigma_i = new Element[ki.length];
        for (int i = 0; i < ki.length; ++i) {
            sigma_i[i] = psw.duplicate().powZn(ki[i].duplicate()).getImmutable();
        }
        /*-------test-------*/
        ClientCryptoTools client = new ClientCryptoTools("a.properties", "P.properties");
        for (int i = 0; i < ki.length; ++i) {
            System.out.printf("sigma_%d checking: ", i);
            System.out.println(client.CheckE(sigma_i[i], client.getGGeneratorP(), psw, PKi[i]));
            System.out.println("psw: " + psw);
            System.out.println("ki: " + ki[i]);
            System.out.println("PKi: " + PKi[i]);
        }
        /*-------test end-------*/
        return sigma_i;
    }

    public void SetSpi(byte[][] sp_i) {
        if (sp_i == null) return;
        this.sp_i = sp_i.clone();
        phi = 0;
        rho = 0;
    }

    public boolean CheckPhiRhoAndIncreaseRho() {
        if (rho > MAX_RHO || phi > MAX_PHI) {
            return false;
        }
        rho++;
        return true;
    }

    public HashMap<String, Element[]> ComputeSigma_iAndRi(Element psw) {
        Element[] sigma_i = this.ComputeSigma_i(psw);
        Element[] r_i = new Element[n];
        for (int i = 0; i < n; ++i) {
            r_i[i] = tools.getRandomElementInZpPlus();
        }
        HashMap<String, Element[]> res = new HashMap<>();
        res.put("sigma_i", sigma_i);
        res.put("r_i", r_i);
        this.r_i = r_i;
        return res;
    }

    public Element[] DecryptRi(byte[][] cipher, int bytesLen) {
        if (cipher == null) return null;
        Element[] ri = new Element[cipher.length];
        for (int i = 0; i < cipher.length; ++i) {
            ri[i] = tools.getElementFromBytesInZp(
                    CryptographicAlgorithm.SM4_Decrypt_ECB(sp_i[i], cipher[i], bytesLen));
        }
        return ri;
    }

    public boolean CheckRi(Element[] r) {
        if (r == null || r_i == null || r.length != r_i.length) return false;
        boolean res = true, temp;
        for (int i = 0; i < r.length; ++i) {
            temp = r[i].isEqual(r_i[i]);
            System.out.printf("r%d checking:  " + temp + "\n", i + 1);
            res &= temp;
        }
        return res;
    }

    public Element[] ComputeAUTi(String token) {
        Element[] res = new Element[n];
        for (int i = 0; i < n; ++i) {
            res[i] = tools.ComputeAUTi(ki[i], token.getBytes());
        }
        return res;
    }

    public byte[][] EncryptToken(Element[] aut, String token) {
        if (aut == null || token == null) return null;
        byte[][] res = new byte[aut.length][];
        byte[] token_bytes = token.getBytes().clone();
        final int aut_len = aut[0].toBytes().length;
        System.out.println("Aut length:  " + aut_len);
        byte[] block = new byte[aut_len + token_bytes.length];
        System.arraycopy(token_bytes, 0, block, aut_len, token_bytes.length);
        for (int i = 0; i < aut.length; ++i) {
            System.arraycopy(aut[i].toBytes(), 0, block, 0, aut_len);
            res[i] = CryptographicAlgorithm.SM4_Encrypt_ECB(sp_i[i], block);
        }
        return res;
    }

    public Element[] RenewShare() {
        Element[][] lx = new Element[n][];
        Element[][] bP = new Element[n][];
        for (int i = 0; i < n; ++i) {
            lx[i] = tools.polynomialLx(t - 1);
            bP[i] = tools.Compute_aPs(lx[i]);
        }

        Element[][] lij = new Element[n][n];
//        | l1(1)  l2(1) l3(1)|
//        | l1(2)  l2(2) l3(2)|
//        | l1(3)  l2(3) l3(3)|
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
//                if (i == j) continue;
                lij[j][i] = tools.ComputeLx(lx[i], j + 1);
            }
        }

        //检查
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i == j) continue;
                System.out.printf("IS%d: verifying IS%d ...  :", i + 1, j + 1);
                System.out.println(tools.CheckLji_bP(lij[i][j], bP[j], i + 1));
            }
        }
        Element[] PKi = new Element[ki.length];
        for (int i = 0; i < ki.length; ++i) {
            ki[i] = tools.RenewKi(ki[i], lij[i]);
            PKi[i] = tools.ComputePKi(ki[i]);
        }
        this.PKi = PKi;
        return PKi;
    }

}
