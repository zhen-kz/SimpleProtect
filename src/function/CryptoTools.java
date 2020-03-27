package function;

import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import org.jetbrains.annotations.NotNull;

/*客户端和IS共同的密码学操作*/
public class CryptoTools {
    protected int rBits = 256;
    protected int qBits = 512;
    protected Pairing pairing;
    protected Field AdditiveGroupG;
    protected Field MultiplicativeGroupGT;
    protected  Field Zp;
    protected  Element GGeneratorP;

    public CryptoTools(){
        pairing = PairingFactory.getPairing(
                new TypeACurveGenerator(rBits, qBits).generate());
        AdditiveGroupG = pairing.getG1();
        MultiplicativeGroupGT = pairing.getGT();
        GGeneratorP = AdditiveGroupG.newRandomElement().getImmutable();
        Zp = pairing.getZr();
    }

    public Element getGGeneratorP() {
        return GGeneratorP.duplicate().getImmutable();
    }


    public Element getRandomElementInZpPlus(){
        Element res;
        do{
            res = Zp.newRandomElement().getImmutable();
        }while(res.isZero());
        return res;
    }

    public static void test(){
        int rBits = 256;
        int qBits = 512;
        TypeACurveGenerator ta = new TypeACurveGenerator(rBits, qBits);
        PairingParameters pg = ta.generate();
        Pairing pairing = PairingFactory.getPairing(pg);
//        Field Zr = pairing.getFieldAt(0);
//        Field G1 = pairing.getFieldAt(1);
//        Field G2 = pairing.getFieldAt(2);
//        Field GT = pairing.getGT(); //getFieldAt(3)
        int degree = pairing.getDegree();
        Element in1, in2, n;
        Field G1 = pairing.getG1(), G2 = pairing.getG2();
        in1 = G1.newRandomElement().getImmutable();
        in2 = G1.newRandomElement().getImmutable();
        n = pairing.getZr().newRandomElement().getImmutable();
        Element out = pairing.pairing(in1.powZn(n.duplicate()), in2);
        Element out2 = pairing.pairing(in1, in2.powZn(n));
        //System.out.println(n.toBigInteger());
//        SecureRandom r = new SecureRandom("abc".getBytes());
//        PolyField pf = new PolyField(r, G1);
//        Polynomial fx = new PolyElement(pf);
        Element x = pairing.getZr().newElement().getImmutable();

        System.out.println(pairing.getZr().getOrder());
        System.out.println(out.toBigInteger());
        System.out.println(out2.toBigInteger());
    }
}
