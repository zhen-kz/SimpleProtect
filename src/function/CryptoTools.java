package function;

import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigInteger;

/*客户端和IS共同的密码学操作*/
public class CryptoTools {
    protected int rBits = 256;
    protected int qBits = 512;
    protected Pairing pairing;
    protected Field AdditiveGroupG;
    protected Field MultiplicativeGroupGT;
    protected Field Zp;
    protected Element GGeneratorP;
    protected BigInteger p;

    //    public CryptoTools(){
//        pairing = PairingFactory.getPairing(
//                new TypeACurveGenerator(rBits, qBits).generate());
//        AdditiveGroupG = pairing.getG1();
//        MultiplicativeGroupGT = pairing.getGT();
//        GGeneratorP = AdditiveGroupG.newElement();
//        Zp = pairing.getZr();
//    }
    public CryptoTools(String a_path, String P_path) {
        pairing = PairingFactory.getPairing(a_path);
        AdditiveGroupG = pairing.getG1();
        MultiplicativeGroupGT = pairing.getGT();
        byte[] buffer = new byte[128];
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(P_path));
            if (bufferedInputStream.read(buffer, 0, buffer.length) == -1) {
                System.out.println("CryptoTools generate P error");
            }
            bufferedInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GGeneratorP = AdditiveGroupG.newElementFromBytes(buffer).getImmutable();
        Zp = pairing.getZr();
        p = AdditiveGroupG.getOrder();
    }

    public Element getGGeneratorP() {
        return GGeneratorP.getImmutable();
    }

    public Element getRandomElementInZpPlus() {
        Element res;
        do {
            res = Zp.newRandomElement().getImmutable();
        } while (res.isZero());
        return res;
    }

    public Element H(final byte[] bytes_array) {
        return AdditiveGroupG.newElement()
                .setFromHash(bytes_array, 0, bytes_array.length)
                .getImmutable();
    }

}
