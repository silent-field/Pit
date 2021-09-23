package com.pit.core.translate;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author gy
 * 使用 google cloud 进行翻译工具类
 */
@Slf4j
public class TranslateUtils {

    private static Translate translate = null;

    static {
        ServiceAccountCredentials serviceAccountCredentials;
        try {
            serviceAccountCredentials = ServiceAccountCredentials.fromStream(TranslateUtils.class.getClassLoader().getResourceAsStream("google-account.json"));
            TranslateOptions build = TranslateOptions.newBuilder().setCredentials(serviceAccountCredentials).build();
            translate = build.getService();
        } catch (IOException e) {
            log.error("init translate error", e);
        }
    }

    public static String translate(String targetLanguageCode, String contents) {
        Translate.TranslateOption option = Translate.TranslateOption.targetLanguage(targetLanguageCode);
        Translate.TranslateOption textOption = Translate.TranslateOption.format("text");
        Translation translation = translate.translate(contents, option, textOption);
        return null == translation ? null : translation.getTranslatedText();
    }

    public static String translate(String sourceLanguage, String targetLanguageCode, String contents) {
        Translate.TranslateOption optionA = Translate.TranslateOption.targetLanguage(targetLanguageCode);
        Translate.TranslateOption optionB = Translate.TranslateOption.sourceLanguage(sourceLanguage);
        Translate.TranslateOption optionC = Translate.TranslateOption.format("text");
        Translation translation = translate.translate(contents, optionA, optionB, optionC);
        return null == translation ? null : translation.getTranslatedText();
    }

    /**
     * 语种探测
     *
     * @param content 被探测文本
     * @return 探测语种
     */
    public static String detectingLanguage(String content) {
        return translate.detect(content).getLanguage().split("-")[0];
    }

}
