package function;

import it.unisa.dia.gas.jpbc.Element;
/*IS的密码学操作*/
public class IdentifyServerCryptoTools extends CryptoTools {
    public Element[] polynomialFx(int degree){
        Element[] fx = new Element[degree];
        fx[0] = getRandomElementInZpPlus().getImmutable();
        for (int i = 1; i < fx.length; ++i){
            fx[i] = Zp.newRandomElement().getImmutable();
        }
        return fx;
    }
}
