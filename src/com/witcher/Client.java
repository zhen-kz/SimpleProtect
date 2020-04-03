package com.witcher;

import function.ClientCryptoTools;
import function.CryptoTools;
import it.unisa.dia.gas.jpbc.Element;

public class Client {
    private ClientCryptoTools tools;

    public Client(){
        tools = new ClientCryptoTools();
    }
    private boolean Register(String ID, String password){
        //混淆密码
        Element r = tools.getRandomElementInZpPlus().getImmutable();
        Element password_confusion = tools.H(password.getBytes()).powZn(r).getImmutable();

        /* 将(ID, password_confusion)发送给全部IS
        *
        *       ......
        * */
        //未完成
        int t = 5;
        Element[] sigma_i = new Element[t], PK_i = new Element[t];
        Element PK;

        //检查σ
        for(int i=0;i<t;++i){
            if(tools.CheckE(sigma_i[i], tools.getGGeneratorP()
                        ,password_confusion, PK_i[i])){
                // ...
            }
            else{
                // ...
                return false;
            }
        }
        //组合获取σu
        Element sigma_u = tools.getSigmaU(sigma_i, r);

        //检查 σu
//        if (tools.CheckE(sigma_u, tools.getGGeneratorP()
//                        ,tools.H(password.getBytes()), PK)){
//
//        }
//        else {
//            return false;
//        }


        return true;
    }

}
