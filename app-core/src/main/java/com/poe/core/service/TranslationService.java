package com.poe.core.service;

import com.poe.cache.dao.TranslationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 英文→中文翻译服务。
 * <p>
 * 翻译优先级：用户自定义翻译 → SQLite 翻译表（含 PoeCharm2 导入数据）→ 原文。
 * 翻译表中不存在时返回原文。
 */
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    private final TranslationDao translationDao;

    public TranslationService(TranslationDao translationDao) {
        this.translationDao = translationDao;
    }

    /**
     * 单条翻译。
     *
     * @param source 英文原文
     * @param domain 翻译领域（item/skill/passive/mod/map）
     * @return 中文翻译，未找到时返回原文
     */
    public String translate(String source, String domain) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        Optional<String> result = translationDao.translate(source, domain);
        if (result.isPresent()) {
            log.debug("Translation found: {} -> {}", source, result.get());
            return result.get();
        }
        log.debug("Translation not found for: {} domain={}", source, domain);
        return source;
    }

    /**
     * 批量翻译。
     *
     * @param sources 源文本列表
     * @param domain  领域
     * @return 源文本 → 翻译文本 的映射，未找到的文本不会出现在返回映射中
     */
    public Map<String, String> batchTranslate(List<String> sources, String domain) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyMap();
        }
        return translationDao.batchTranslate(sources, domain);
    }

    /**
     * 导入翻译数据。
     *
     * @param translations 源文本 → 翻译文本 的映射
     * @param domain       领域
     */
    public void importTranslations(Map<String, String> translations, String domain) {
        if (translations == null || translations.isEmpty()) {
            return;
        }
        translationDao.batchSave(translations, domain);
        log.info("Imported {} translations for domain {}", translations.size(), domain);
    }
}
