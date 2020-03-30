package function;

import it.unisa.dia.gas.jpbc.Element;
/*IS的密码学操作*/
public class IdentifyServerCryptoTools extends CryptoTools {
    public Element[] polynomialFx(int degree){
        Element[] fx = new Element[degree + 1];
        fx[0] = getRandomElementInZpPlus().getImmutable();
        for (int i = 1; i < fx.length; ++i){
            fx[i] = Zp.newRandomElement().getImmutable();
        }
        return fx;
    }
    public Element ComputeFx(Element[] fx, int x){
        if (fx == null) return null;
        Element res = fx[0].getImmutable();
        Element xx = Zp.newElement(x).getImmutable();
        for(int i = 1; i < fx.length; ++i){
            res = res.add(fx[i].duplicate().mul(xx.powZn(Zp.newElement(i))));
        }
        return res;
    }
    //fj: Zp, aP: G
    public boolean CheckF_aP(Element[] fj, Element[] aP, int i){
        if(fj == null || aP == null) return false;
        Element ii, order;
        Element left, right;
        left = ComputeFx(fj, i);
        //compute fj(i)P
        left = getGGeneratorP().powZn(left).getImmutable();
        right = aP[0].getImmutable();
        ii = Zp.newElement(i).getImmutable();
        for(int x = 1; x < aP.length; ++x){
            order = Zp.newElement(x).getImmutable();
            right = right.mul(aP[x].duplicate()
                    .powZn(ii.duplicate().powZn(order)));
        }
        return left.isEqual(right);
    }
    public Element ComputeKi(Element[] fx_res){
        if (fx_res == null) return null;
        Element ki = fx_res[0].getImmutable();
        for(int i=1;i<fx_res.length;++i){
            ki = ki.add(fx_res[i].duplicate()).getImmutable();
        }
        return ki;
    }
    public Element ComputePKi(Element si){
        if (si == null) return null;
        return getGGeneratorP().powZn(si.duplicate()).getImmutable();
    }
    public Element ComputePK(Element[] a0P){
        if (a0P == null) return null;
        Element pk = a0P[0].getImmutable();
        for(int i = 1; i < a0P.length; ++i){
            pk = pk.mul(a0P[i]).getImmutable();
        }
        return pk;
    }
}
