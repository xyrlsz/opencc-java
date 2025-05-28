package com.xyrlsz;


import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OpenCC {

    private String conversion;
    private String description;
    private List<Group> dictChains = new ArrayList<>();
    private static Map<String, OpenCC> instances = new HashMap<>();
    private static final Set<String> CONVERSIONS = new HashSet<>(Arrays.asList(
            "hk2s", "s2hk", "s2t", "s2tw", "s2twp", "t2hk", "t2s", "t2tw", "tw2s", "tw2sp", "s2hk-finance"
    ));

    public OpenCC(String conversion) throws Exception {
        if (!CONVERSIONS.contains(conversion)) {
            throw new IllegalArgumentException(conversion + " not valid");
        }
        this.conversion = conversion;
        initDict();
    }

    private static void initInstances() throws Exception {
        for (String type : CONVERSIONS) {
            OpenCC oc = newInstance(type);
            instances.put(type, oc);
        }
    }

    public static synchronized OpenCC getInstance(String type) throws Exception {
        if (instances.isEmpty()) {
            initInstances();
        }
        return instances.get(type);
    }

    public static OpenCC newInstance(String conversion) throws Exception {
        if (!CONVERSIONS.contains(conversion)) {
            throw new IllegalArgumentException(conversion + " not valid");
        }
        OpenCC cc = new OpenCC(conversion);
        cc.initDict();
        return cc;
    }


    public String convert(String input) throws Exception {
        for (Group group : dictChains) {
            List<String> tokens = new ArrayList<>();
            int i = 0;
            char[] chars = input.toCharArray();
            while (i < chars.length) {
                String token = null;
                int maxLen = 0;

                for (Map<String, String> dict : group.dicts) {
                    for (int len = 1; len <= 10 && i + len <= chars.length; len++) {
                        String substr = new String(chars, i, len);
                        if (dict.containsKey(substr) && len > maxLen) {
                            token = dict.get(substr);
                            maxLen = len;
                        }
                    }
                }

                if (token != null) {
                    tokens.add(token);
                    i += maxLen;
                } else {
                    tokens.add(String.valueOf(chars[i]));
                    i++;
                }
            }
            input = String.join("", tokens);
        }
        return input;
    }

    private void initDict() throws Exception {
        InputStream is = getClass().getResourceAsStream("/config/" + conversion + ".json");
        if (is == null) throw new FileNotFoundException("Config not found: " + conversion);

        String jsonText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        JsonObject config = JsonParser.parseString(jsonText).getAsJsonObject();

        this.description = config.get("name").getAsString();
        JsonArray chain = config.getAsJsonArray("conversion_chain");

        for (JsonElement element : chain) {
            JsonObject dictObj = element.getAsJsonObject().getAsJsonObject("dict");
            Group group = addDictChain(dictObj);
            dictChains.add(group);
        }
    }

    private Group addDictChain(JsonObject dictJson) throws Exception {
        String type = dictJson.get("type").getAsString();
        Group group = new Group();

        switch (type) {
            case "group":
                JsonArray dicts = dictJson.getAsJsonArray("dicts");
                for (JsonElement el : dicts) {
                    Group subGroup = addDictChain(el.getAsJsonObject());
                    group.files.addAll(subGroup.files);
                    group.dicts.addAll(subGroup.dicts);
                }
                break;
            case "txt":
            case "ocd2":
                String fileName = dictJson.get("file").getAsString().replace(".ocd2", ".txt");
                InputStream is = getClass().getResourceAsStream("/dictionary/" + fileName);
                if (is == null) throw new FileNotFoundException("Dictionary not found: " + fileName);
                Map<String, String> map = loadDict(is);
                group.files.add(fileName);
                group.dicts.add(map);
                break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }

        return group;
    }

    private Map<String, String> loadDict(InputStream is) throws IOException {
        Map<String, String> dict = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && !line.isEmpty()) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        dict.putIfAbsent(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        }
        return dict;
    }


    static class Group {
        List<String> files = new ArrayList<>();
        List<Map<String, String>> dicts = new ArrayList<>();
    }
}
