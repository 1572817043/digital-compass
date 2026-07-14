package com.a0000.digicompass.modules.ai.embedding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class LocalTextEmbeddingService {

    private static final int DIMENSION = 128;
    private static final String MODEL_NAME = "local-keyword-v1";

    public String modelName() {
        return MODEL_NAME;
    }

    public double[] embed(String text) {
        double[] vector = new double[DIMENSION];
        String normalized = expandDomainTerms(text == null ? "" : text.toLowerCase(Locale.ROOT));
        List<String> tokens = tokens(normalized);
        for (String token : tokens) {
            add(vector, "token:" + token, 2.2);
        }
        for (int i = 0; i < normalized.length(); i++) {
            char current = normalized.charAt(i);
            if (!Character.isWhitespace(current)) {
                add(vector, "char:" + current, 0.8);
            }
            if (i + 1 < normalized.length()) {
                String gram = normalized.substring(i, i + 2).trim();
                if (gram.length() == 2) {
                    add(vector, "gram:" + gram, 1.5);
                }
            }
        }
        return normalize(vector);
    }

    private List<String> tokens(String text) {
        List<String> result = new ArrayList<>();
        Pattern.compile("[,，、。；;\\s]+")
                .splitAsStream(text)
                .map(String::trim)
                .filter(item -> item.length() >= 2)
                .forEach(result::add);
        return result;
    }

    private String expandDomainTerms(String text) {
        StringBuilder builder = new StringBuilder(text);
        if (containsAny(text, "拍照", "摄影", "影像", "相机", "长焦", "人像")) builder.append(" 拍照 摄影 影像 相机");
        if (containsAny(text, "旅行", "出游", "旅游", "vlog")) builder.append(" 旅行 出游 vlog");
        if (containsAny(text, "游戏", "电竞", "高帧率", "性能")) builder.append(" 游戏 电竞 性能");
        if (containsAny(text, "办公", "学习", "网课", "文档")) builder.append(" 办公 学习 文档");
        if (containsAny(text, "续航", "电池", "充电")) builder.append(" 续航 电池 充电");
        if (containsAny(text, "轻薄", "便携", "小屏")) builder.append(" 轻薄 便携");
        if (containsAny(text, "预算", "价格", "便宜", "性价比")) builder.append(" 预算 价格 性价比");
        return builder.toString();
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private void add(double[] vector, String token, double weight) {
        int index = Math.floorMod(token.hashCode(), vector.length);
        vector[index] += weight;
    }

    private double[] normalize(double[] vector) {
        double norm = 0;
        for (double value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        if (norm == 0) return vector;
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }
        return vector;
    }
}
