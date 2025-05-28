package com.xyrlsz;


public class Main {
    public static void main(String[] args) {
        try {
            OpenCC openCC = OpenCC.getInstance("s2t");
            String s = openCC.convert(" 拷贝出来做了自定义修改，那也要保证配置结构与 OpenCC 规范一致");
            System.out.println(s);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
