package org.hv.cipher;

import org.hv.Application;
import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.utils.EncryptUtil;
import org.hv.pocket.utils.sm4.Sm4Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class EncryptUtilTest {

    @Test
    public void testDes() {
        String code = EncryptUtil.encrypt(EncryptType.DES, "王尔尔");
        String name = EncryptUtil.decrypt(EncryptType.DES, code);
        System.out.println(code);
        System.out.println(name);
    }
}
