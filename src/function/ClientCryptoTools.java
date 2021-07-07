package function;

import it.unisa.dia.gas.jpbc.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/*客户端的密码学操作*/
public class ClientCryptoTools extends CryptoTools {
    public ClientCryptoTools(String a_path, String P_path) {
        super(a_path, P_path);
    }

    public Element e(@NotNull final Element a, @NotNull final Element b) {
        return pairing.pairing(a.duplicate(), b.duplicate()).getImmutable();
    }

    public boolean CheckE(@NotNull final Element a1,
                          @NotNull final Element a2,
                          @NotNull final Element b1,
                          @NotNull final Element b2) {
        return e(a1, a2).isEqual(e(b1, b2));
    }

    private Element Combine(@NotNull Element[] sigma_i, @NotNull int[] index) {
        if (sigma_i.length != index.length) return null;
        //计算 ω
        Element[] omega = new Element[index.length];
        Element omega_temp, element_l;
        for (int h = 0; h < index.length; ++h) {
            omega_temp = Zp.newOneElement().getImmutable();
            for (int l = 0; l < index.length; ++l) {
                if (l + 1 == h + 1) continue;
                element_l = Zp.newElement(index[l]).getImmutable();
                omega_temp = omega_temp.mul(element_l.div(element_l.sub(Zp.newElement(index[h])))).getImmutable();
            }
            omega[h] = omega_temp;
        }
        // 计算 σu
        Element omega_sigma = AdditiveGroupG.newOneElement();
        for (int i = 0; i < sigma_i.length; ++i) {
            omega_sigma = omega_sigma.mul(sigma_i[i].powZn(omega[i])).getImmutable();
        }
        return omega_sigma.getImmutable();
    }

    public Element ComputeSigmaU(@NotNull Element[] sigma_i, @NotNull int[] index, @NotNull final Element r) {
        if (sigma_i.length != index.length) return null;
        Element omega_sigma = this.Combine(sigma_i, index);
        return omega_sigma.powZn(r.invert()).getImmutable();
    }

//    public Element getSigmaU(@NotNull Element[] sigma_i, @NotNull final Element r){
//        //计算 ω
//        Element[] omega = new Element[sigma_i.length];
//        Element omega_temp, element_l;
//        for(int h=0; h < sigma_i.length; ++h){
//            omega_temp = Zp.newOneElement().getImmutable();
//            for(int l = 0; l < sigma_i.length; ++l){
//                if (l+1 == h+1) continue;
//                element_l = Zp.newElement(l+1).getImmutable();
//                omega_temp = omega_temp.mul(element_l.div(element_l.sub(Zp.newElement(h+1)))).getImmutable();
//            }
//            omega[h] = omega_temp;
//        }
//        // 计算 σu
//        Element omega_sigma = AdditiveGroupG.newOneElement();
//        for(int i = 0; i < sigma_i.length; ++i){
//            omega_sigma = omega_sigma.mul(sigma_i[i].powZn(omega[i])).getImmutable();
//        }
//        return omega_sigma.powZn(r.invert()).getImmutable();
//    }

    public Element ComputeAutU(@NotNull Element[] aut_i, @NotNull int[] index) {
        if (aut_i.length != index.length) return null;
        return this.Combine(aut_i, index);
    }

    public Element getElementFromBytesInG(@NotNull byte[] buffer){
        return AdditiveGroupG.newElementFromBytes(buffer).getImmutable();
    }
}
