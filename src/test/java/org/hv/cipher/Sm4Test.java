package org.hv.cipher;

import org.hv.Application;
import org.hv.pocket.utils.sm4.Sm4Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class Sm4Test {

    @Test
    public void testSM4() {
        String plainText = "root";

        Sm4Utils sm4 = new Sm4Utils();
        sm4.setSecretKey("sward18713839007");
        sm4.setHexString(false);

        System.out.println("ECB模式");
        String cipherText = sm4.encryptDataEcb(plainText);
        System.out.println("密文: " + cipherText);

        plainText = sm4.decryptDataEbc(cipherText);
        System.out.println("明文: " + plainText);

        System.out.println("CBC模式");
        sm4.setIv("UISwD9fW6cFh9SNS");
        cipherText = sm4.encryptDataCbc(plainText);
        System.out.println("密文: " + cipherText);

        plainText = sm4.decryptDataCbc(cipherText);
        System.out.println("明文: " + plainText);
    }
}
