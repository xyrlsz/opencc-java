package com.xyrlsz;

import java.io.IOException;

public class Xyropencc {
    public static String t2S(String s) throws Exception {
        OpenCC oc = OpenCC.getInstance("s2t");
        return oc.convert(s);
    }

    public static String s2T(String s) throws Exception {
        OpenCC oc = OpenCC.getInstance("t2s");
        return oc.convert(s);
    }

    public static String s2TWP(String s) throws Exception {
        OpenCC oc = OpenCC.getInstance("s2twp");
        return oc.convert(s);
    }

    public static String tW2SP(String s) throws Exception {
        OpenCC oc = OpenCC.getInstance("tw2sp");
        return oc.convert(s);
    }
}
