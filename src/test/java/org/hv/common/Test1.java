package org.hv.common;

import org.hv.Application;
import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.flib.DecryptFunctionLib;
import org.hv.pocket.flib.EncryptFunctionLib;
import org.hv.pocket.utils.EncryptUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hv.pocket.constant.RegexString.SQL_PARAMETER_REGEX;

/**
 * @author wujianchuan 2019/1/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class Test1 {
    @Test
    public void test1() throws SQLException {
        String key = "sward18713839007";
        String columnValue = EncryptFunctionLib.getEncryptFunction(EncryptType.DES).apply("C-001", key);
        System.out.println(columnValue);
        String code = DecryptFunctionLib.getDecryptFunction(EncryptType.DES).apply(columnValue, key);
        System.out.println(code);
    }

    @Test
    public void test2() throws SQLException {
        String key = "sward18713839007";
        String columnValue = EncryptFunctionLib.getEncryptFunction(EncryptType.SM4_CEB).apply("root", key);
        System.out.println(columnValue);
        String code = DecryptFunctionLib.getDecryptFunction(EncryptType.SM4_CEB).apply(columnValue, key);
        System.out.println(code);
    }

    @Test
    public void test3() throws SQLException {
        String key = "sward18713839007";
        String columnValue = EncryptFunctionLib.getEncryptFunction(EncryptType.SM4_CBC).apply("hello word", key);
        System.out.println(columnValue);
        String code = DecryptFunctionLib.getDecryptFunction(EncryptType.SM4_CBC).apply(columnValue, key);
        System.out.println(code);
    }

    @Test
    public void test4() {
        String columnValue = EncryptUtil.encrypt(EncryptType.SM4_CBC, "hello word");
        System.out.println(columnValue);
        String code = EncryptUtil.decrypt(EncryptType.SM4_CBC, columnValue);
        System.out.println(code);
    }

    @Test
    public void test5() {
        Pattern pattern = Pattern.compile(SQL_PARAMETER_REGEX);
        Matcher matcher = pattern.matcher(":ABC_ad :123");
        while (matcher.find()) {
            String regexString = matcher.group();
            String name = regexString.substring(1);
            System.out.println(name);
        }
    }
}
