package com.poti.search.controller;

import com.poti.common.R;
import com.poti.common.redis.service.SearchCacheService;
import com.poti.search.client.QuestionClient;
import com.poti.search.dto.QuestionDocumentDTO;
import com.poti.search.entity.QuestionDocument;
import com.poti.search.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired(required = false)
    private QuestionRepository questionRepository;

    @Autowired
    private SearchCacheService searchCacheService;

    @Autowired
    private QuestionClient questionClient;

    @Value("${elasticsearch.enabled:false}")
    private boolean esEnabled;

    @GetMapping("/question")
    public R<Map<String, Object>> searchQuestion(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) Integer difficulty,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "20") Integer pageSize,
                                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (keyword != null && !keyword.trim().isEmpty()) {
                if (userId != null) {
                    searchCacheService.addSearchHistory(userId, keyword);
                }
                searchCacheService.incrementHotSearch(keyword);
            }

            if (esEnabled && questionRepository != null) {
                try {
                    return searchWithElasticsearch(keyword, categoryId, pageNum, pageSize);
                } catch (Exception esEx) {
                    log.warn("ES 检索不可用，自动降级数据库检索: {}", esEx.getMessage());
                }
            }

            return searchFromDatabase(keyword, categoryId, difficulty, pageNum, pageSize);
        } catch (Exception e) {
            log.error("搜索题目失败", e);
            return R.success(getEmptyResult());
        }
    }

    /**
     * internal：题目搜索（AI 助手 poti-agent 调用）：
     * 与 /search/question 相同的检索逻辑，但不写搜索历史、不加热热搜
     */
    @GetMapping("/internal/question")
    public R<Map<String, Object>> searchQuestionInternal(@RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) Long categoryId,
                                                         @RequestParam(required = false) Integer difficulty,
                                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            if (esEnabled && questionRepository != null) {
                try {
                    return searchWithElasticsearch(keyword, categoryId, pageNum, pageSize);
                } catch (Exception esEx) {
                    log.warn("ES 检索不可用，自动降级数据库检索: {}", esEx.getMessage());
                }
            }
            return searchFromDatabase(keyword, categoryId, difficulty, pageNum, pageSize);
        } catch (Exception e) {
            log.error("internal 搜索题目失败", e);
            return R.success(getEmptyResult());
        }
    }

    /**
     * 数据库降级检索：ES 未启用或运行中不可用时走 question 服务 /question/search（LIKE 匹配题干/内容/解析/标签/分类名）
     */
    private R<Map<String, Object>> searchFromDatabase(String keyword, Long categoryId, Integer difficulty, Integer pageNum, Integer pageSize) {
        try {
            R<Map<String, Object>> dbResult = questionClient.searchQuestions(keyword, categoryId, difficulty, pageNum, pageSize);
            if (dbResult != null && dbResult.getData() != null) {
                return R.success(dbResult.getData());
            }
        } catch (Exception e) {
            log.error("数据库降级检索失败", e);
        }
        return R.success(getEmptyResult());
    }

    private R<Map<String, Object>> searchWithElasticsearch(String keyword, Long categoryId, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<QuestionDocument> page;

        if (keyword == null || keyword.trim().isEmpty()) {
            page = questionRepository.findAll(pageable);
        } else if (categoryId != null) {
            page = questionRepository.searchByKeywordAndCategoryId(keyword, categoryId, pageable);
        } else {
            page = questionRepository.searchByKeyword(keyword, pageable);
        }
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (QuestionDocument doc : page.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", doc.getId());
            item.put("categoryId", doc.getCategoryId());
            item.put("title", doc.getTitle());
            item.put("content", doc.getContent());
            item.put("type", doc.getType());
            item.put("difficulty", doc.getDifficulty());
            item.put("answer", doc.getAnswer());
            item.put("analysis", doc.getAnalysis());
            item.put("tags", doc.getTags());
            item.put("categoryName", doc.getCategoryName());
            
            List<String> options = new ArrayList<>();
            if (doc.getOptionA() != null) options.add(doc.getOptionA());
            if (doc.getOptionB() != null) options.add(doc.getOptionB());
            if (doc.getOptionC() != null) options.add(doc.getOptionC());
            if (doc.getOptionD() != null) options.add(doc.getOptionD());
            if (!options.isEmpty()) {
                item.put("options", options);
            }
            
            list.add(item);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotalElements());
        result.put("list", list);
        
        return R.success(result);
    }

    private Map<String, Object> getEmptyResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("list", Collections.emptyList());
        return result;
    }

    @GetMapping("/questions")
    public R<Map<String, Object>> searchQuestions(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Long categoryId,
                                                   @RequestParam(required = false) Integer difficulty,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "20") Integer pageSize,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return searchQuestion(keyword, categoryId, difficulty, pageNum, pageSize, userId);
    }

    @GetMapping("/history")
    public R<List<Object>> getSearchHistory(@RequestHeader("X-User-Id") Long userId) {
        List<Object> history = searchCacheService.getSearchHistory(userId);
        return R.success(history);
    }

    @DeleteMapping("/history")
    public R<Void> clearSearchHistory(@RequestHeader("X-User-Id") Long userId) {
        searchCacheService.clearSearchHistory(userId);
        return R.success();
    }

    @GetMapping("/hot")
    public R<Set<Object>> getHotSearches(@RequestParam(defaultValue = "10") Integer limit) {
        Set<Object> hotSearches = searchCacheService.getHotSearches(limit);
        return R.success(hotSearches);
    }

    /**
     * 搜索联想：根据输入前缀从题库（题干/标签/分类名）提取候选词
     */
    @GetMapping("/suggest")
    public R<List<String>> suggest(@RequestParam String keyword) {
        List<String> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.success(result);
        }
        String kw = keyword.trim();
        String lower = kw.toLowerCase();
        try {
            if (esEnabled && questionRepository != null) {
                Page<QuestionDocument> page = questionRepository.suggestByKeyword(kw, PageRequest.of(0, 50));
                for (QuestionDocument doc : page.getContent()) {
                    collectCandidate(result, doc.getCategoryName(), lower);
                    collectTags(result, doc.getTags(), lower);
                    String title = doc.getTitle();
                    if (title != null && title.length() <= 25 && title.toLowerCase().contains(lower)) {
                        collectCandidate(result, title.trim(), lower);
                    }
                    if (result.size() >= 10) {
                        break;
                    }
                }
            } else {
                collectSuggestFromDatabase(result, kw, lower);
            }
        } catch (Exception e) {
            log.warn("ES 联想不可用，降级数据库联想: {}", e.getMessage());
            collectSuggestFromDatabase(result, kw, lower);
        }
        return R.success(result);
    }

    /**
     * 数据库降级联想：ES 未启用或不可用时，从 question 服务的 LIKE 检索结果中提取候选词（分类名/标签/短题干）
     */
    private void collectSuggestFromDatabase(List<String> result, String keyword, String lower) {
        try {
            R<Map<String, Object>> db = questionClient.searchQuestions(keyword, null, null, 1, 50);
            if (db == null || db.getData() == null) {
                return;
            }
            Object listObj = db.getData().get("list");
            if (!(listObj instanceof List)) {
                return;
            }
            for (Object o : (List<?>) listObj) {
                if (result.size() >= 10) {
                    return;
                }
                if (!(o instanceof Map)) {
                    continue;
                }
                Map<?, ?> item = (Map<?, ?>) o;
                Object title = item.get("title");
                if (title instanceof String) {
                    String t = ((String) title).trim();
                    if (t.length() <= 25) {
                        collectCandidate(result, t, lower);
                    }
                }
                Object tags = item.get("tags");
                if (tags instanceof String) {
                    collectTags(result, (String) tags, lower);
                }
                Object categoryName = item.get("categoryName");
                if (categoryName instanceof String) {
                    collectCandidate(result, (String) categoryName, lower);
                }
            }
        } catch (Exception e) {
            log.error("数据库联想降级失败", e);
        }
    }

    private void collectCandidate(List<String> result, String candidate, String lower) {
        if (candidate == null || candidate.trim().isEmpty()) {
            return;
        }
        String c = candidate.trim();
        if (c.toLowerCase().contains(lower) && !result.contains(c)) {
            result.add(c);
        }
    }

    private void collectTags(List<String> result, String tags, String lower) {
        if (tags == null || tags.trim().isEmpty()) {
            return;
        }
        // tags 兼容 JSON 数组字符串与分隔符字符串两种存储格式
        String cleaned = tags.replace("[", " ").replace("]", " ").replace("\"", " ");
        for (String t : cleaned.split("[,，、;；|/]")) {
            collectCandidate(result, t, lower);
            if (result.size() >= 10) {
                return;
            }
        }
    }

    @PostMapping("/sync")
    public R<Void> syncQuestion(@RequestBody QuestionDocumentDTO dto) {
        if (!esEnabled || questionRepository == null) {
            return R.success();
        }
        try {
            QuestionDocument doc = convertToDocument(dto);
            questionRepository.save(doc);
            log.info("同步题目到ES成功: {}", doc.getId());
            return R.success();
        } catch (Exception e) {
            log.error("同步题目失败", e);
            return R.error("同步题目失败");
        }
    }

    @PostMapping("/sync/batch")
    public R<Void> syncQuestions(@RequestBody List<QuestionDocumentDTO> dtos) {
        if (!esEnabled || questionRepository == null) {
            return R.success();
        }
        try {
            List<QuestionDocument> docs = new ArrayList<>();
            for (QuestionDocumentDTO dto : dtos) {
                docs.add(convertToDocument(dto));
            }
            questionRepository.saveAll(docs);
            log.info("批量同步题目到ES成功: {} 条", docs.size());
            return R.success();
        } catch (Exception e) {
            log.error("批量同步题目失败", e);
            return R.error("批量同步题目失败");
        }
    }

    private QuestionDocument convertToDocument(QuestionDocumentDTO dto) {
        QuestionDocument doc = new QuestionDocument();
        doc.setId(dto.getId());
        doc.setCategoryId(dto.getCategoryId());
        doc.setType(dto.getType());
        doc.setTitle(dto.getTitle());
        doc.setContent(dto.getContent());
        doc.setOptionA(dto.getOptionA());
        doc.setOptionB(dto.getOptionB());
        doc.setOptionC(dto.getOptionC());
        doc.setOptionD(dto.getOptionD());
        doc.setAnswer(dto.getAnswer());
        doc.setAnalysis(dto.getAnalysis());
        doc.setTags(dto.getTags());
        doc.setDifficulty(dto.getDifficulty());
        doc.setCategoryName(dto.getCategoryName());
        return doc;
    }

    @DeleteMapping("/question/{id}")
    public R<Void> deleteQuestion(@PathVariable Long id) {
        if (!esEnabled || questionRepository == null) {
            return R.success();
        }
        try {
            questionRepository.deleteById(id);
            return R.success();
        } catch (Exception e) {
            log.error("删除题目失败", e);
            return R.error("删除题目失败");
        }
    }

    @PostMapping("/rebuild-index")
    public R<String> rebuildIndex() {
        if (!esEnabled || questionRepository == null) {
            return R.error("Elasticsearch未启用");
        }
        try {
            questionRepository.deleteAll();
            return R.success("索引已清空，请调用题目服务同步数据");
        } catch (Exception e) {
            log.error("重建索引失败", e);
            return R.error("重建索引失败");
        }
    }
}
