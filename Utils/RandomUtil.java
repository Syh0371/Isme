package cn.together.common.core.util;

import java.util.Random;


public class RandomUtil {

    public static String randomStr(int length) {
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
}
