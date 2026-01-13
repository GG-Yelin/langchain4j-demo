package org.example.langchain4jdemo;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;

public class StreamEncodingTest {
    
    @Test
    public void testUtf8Encoding() {
        String chinese = "你好世界";
        String english = "Hello World";
        
        // 测试UTF-8编码
        byte[] chineseBytes = chinese.getBytes(StandardCharsets.UTF_8);
        byte[] englishBytes = english.getBytes(StandardCharsets.UTF_8);
        
        System.out.println("中文字符串: " + chinese);
        System.out.println("中文UTF-8字节数: " + chineseBytes.length);
        System.out.println("中文字节内容: " + bytesToHex(chineseBytes));
        
        System.out.println("\n英文字符串: " + english);
        System.out.println("英文UTF-8字节数: " + englishBytes.length);
        System.out.println("英文字节内容: " + bytesToHex(englishBytes));
        
        // 测试解码
        String decodedChinese = new String(chineseBytes, StandardCharsets.UTF_8);
        String decodedEnglish = new String(englishBytes, StandardCharsets.UTF_8);
        
        System.out.println("\n解码后的中文: " + decodedChinese);
        System.out.println("解码后的英文: " + decodedEnglish);
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
