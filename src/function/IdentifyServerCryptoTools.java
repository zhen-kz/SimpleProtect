package function;

import com.witcher.IdentifyServer;
import it.unisa.dia.gas.jpbc.Element;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

/*IS的密码学操作*/
public class IdentifyServerCryptoTools extends CryptoTools {
    public IdentifyServerCryptoTools(String a_path, String P_path) {
        super(a_path, P_path);
    }

    //    public IdentifyServerCryptoTools(){}
    public Element[] polynomialFx(int degree) {
        Element[] fx = new Element[degree + 1];
        fx[0] = getRandomElementInZpPlus().getImmutable();
        for (int i = 1; i < fx.length; ++i) {
            fx[i] = Zp.newRandomElement().getImmutable(); //随机生成aij属于Zp*
        }
        return fx;
    }

    public Element[] polynomialLx(int degree) {
        Element[] fx = new Element[degree];
        fx[0] = getRandomElementInZpPlus().getImmutable();
        for (int i = 1; i < fx.length; ++i) {
            fx[i] = Zp.newRandomElement().getImmutable();
        }
        return fx;
    }

    public Element ComputeFx(Element[] fx, int x) {
        if (fx == null) return null;
        Element res = fx[0].getImmutable();
        Element xx = Zp.newElement(x).getImmutable();
        for (int i = 1; i < fx.length; ++i) {
            res = res.add(fx[i].duplicate().mul(xx.powZn(Zp.newElement(i))));
        }
        return res.getImmutable();
    }

    public Element ComputeLx(Element[] lx, int x) {
        if (lx == null) return null;
        Element xx = Zp.newElement(x).getImmutable();
        Element res = lx[0].duplicate().mul(xx.powZn(Zp.newElement(1)));
        for (int i = 1; i < lx.length; ++i) {
            res = res.add(lx[i].duplicate().mul(xx.powZn(Zp.newElement(i+1))));
        }
        return Zp.newElement(res.toBigInteger().mod(p)).getImmutable();
    }

    public Element[] Compute_aPs(Element[] fx) {
        if (fx == null || fx.length <= 0) return null;
        Element[] res = new Element[fx.length];
        Element P = getGGeneratorP().getImmutable();
        for (int i = 0; i < fx.length; ++i) {
            res[i] = P.powZn(fx[i].duplicate()).getImmutable();
        }
        return res;
    }

    //fj: Zp, aP: G
    public boolean CheckFji_aP(Element fji, Element[] aP, int i) {
        if (fji == null || aP == null) return false;
        Element ii, order;
        Element left, right;
        left = fji.getImmutable();
        //compute fj(i)P
        left = getGGeneratorP().powZn(left).getImmutable();
        right = aP[0].getImmutable();
        ii = Zp.newElement(i).getImmutable();
        for (int x = 1; x < aP.length; ++x) {
            order = Zp.newElement(x).getImmutable();
            right = right.mul(aP[x].duplicate()
                    .powZn(ii.duplicate().powZn(order)));
        }
        return left.isEqual(right);
    }

    public boolean CheckLji_bP(Element Lji, Element[] bP, int i) {
        if (Lji == null || bP == null) return false;
        Element ii, order;
        Element left, right;
        left = Lji.getImmutable();
        //compute lj(i)P
        left = getGGeneratorP().powZn(left).getImmutable();
        ii = Zp.newElement(i).getImmutable();
        right = bP[0].duplicate().powZn(ii.duplicate().powZn(Zp.newElement(1)));
        for (int x = 1; x < bP.length; ++x) {
            order = Zp.newElement(x+1);
            right = right.mul(bP[x].duplicate()
                    .powZn(ii.duplicate().powZn(order)));
        }
        return left.isEqual(right);
    }

    public Element ComputeKi(Element[] fx_res) {
        if (fx_res == null) return null;
        Element ki = fx_res[0].getImmutable();
        for (int i = 1; i < fx_res.length; ++i) {
            ki = ki.add(fx_res[i].duplicate()).getImmutable();
        }
        return ki;
    }

    public Element ComputePKi(Element si) {
        if (si == null) return null;
        return getGGeneratorP().powZn(si.duplicate()).getImmutable();
    }

    public Element ComputePK(Element[] a0P) {
        if (a0P == null) return null;
        Element pk = a0P[0].getImmutable();
        for (int i = 1; i < a0P.length; ++i) {
            pk = pk.mul(a0P[i]).getImmutable();
        }
        return pk;
    }

    public Element getElementFromBytesInZp(@NotNull byte[] buffer){
        return Zp.newElementFromBytes(buffer).getImmutable();
    }

    public Element ComputeAUTi(Element ki, byte[] token){
        if (ki == null || token == null) return null;
        return H(token).powZn(ki.duplicate()).getImmutable();
    }

    public Element RenewKi(Element ki, Element[] fx_res){
        if (ki == null || fx_res == null) return null;
        return ki.duplicate().add(this.ComputeKi(fx_res)).getImmutable();
    }
}
