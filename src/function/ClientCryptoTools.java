package function;

import it.unisa.dia.gas.jpbc.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/*客户端的密码学操作*/
public class ClientCryptoTools extends CryptoTools {
    public Element e(@NotNull final Element a, @NotNull final Element b){
        return pairing.pairing(a.duplicate(), b.duplicate()).getImmutable();
    }

    public Element H(final byte[] bytes_array){
        return AdditiveGroupG.newElement()
                .setFromHash(bytes_array, 0, bytes_array.length)
                .getImmutable();
    }

    public boolean CheckE(@NotNull final Element a1,
                          @NotNull final Element a2,
                          @NotNull final Element b1,
                          @NotNull final Element b2){
        return e(a1, a2).isEqual(e(b1, b2));
    }

    public Element getSigmaU(@NotNull Element[] sigma_i,@NotNull final Element r){
        //计算 ω
        Element[] omega = new Element[sigma_i.length];
        Element omega_temp, element_l;
        for(int h=0; h < sigma_i.length; ++h){
            omega_temp = Zp.newOneElement().getImmutable();
            for(int l = 0; l < sigma_i.length; ++l){
                if (l+1 == h+1) continue;
                element_l = Zp.newElement(l+1).getImmutable();
                omega_temp = omega_temp.mul(element_l.div(element_l.sub(Zp.newElement(h+1)))).getImmutable();
            }
            omega[h] = omega_temp;
        }
        // 计算 σu
        Element omega_sigma = AdditiveGroupG.newOneElement();
        for(int i = 0; i < sigma_i.length; ++i){
            omega_sigma = omega_sigma.mul(sigma_i[i].powZn(omega[i])).getImmutable();
        }
        return omega_sigma.powZn(r.invert()).getImmutable();
    }
}
