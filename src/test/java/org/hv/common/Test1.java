package org.hv.common;

import org.hv.Application;
import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.flib.EncryptFunctionLib;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;
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
        byte[] persistenceKey = "sward9007".getBytes(StandardCharsets.UTF_8);
        byte[] columnBytesValue = EncryptFunctionLib.getEncryptFunction(EncryptType.DES).apply(Base64.getDecoder().decode("20101401".replaceAll(" +", "+").replace("\r\n", "")), persistenceKey);
        System.out.println(Base64.getEncoder().encodeToString(columnBytesValue));
    }

    @Test
    public void test2() {
        Pattern pattern = Pattern.compile(SQL_PARAMETER_REGEX);
        Matcher matcher = pattern.matcher(":ABC_ad :123");
        while (matcher.find()) {
            String regexString = matcher.group();
            String name = regexString.substring(1);
            System.out.println(name);
        }
    }
}
