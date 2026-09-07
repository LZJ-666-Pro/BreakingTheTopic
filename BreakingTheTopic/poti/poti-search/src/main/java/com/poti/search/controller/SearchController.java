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
                                                  @RequestParam(required = false) String type,
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
                return searchWithElasticsearch(keyword, type, pageNum, pageSize);
            }
            
            R<Map<String, Object>> dbResult = questionClient.searchQuestions(keyword, null, pageNum, pageSize);
            
            if (dbResult != null && dbResult.getData() != null) {
                return R.success(dbResult.getData());
            }
            
            return R.success(getEmptyResult());
        } catch (Exception e) {
            log.error("搜索题目失败", e);
            return R.success(getEmptyResult());
        }
    }

    private R<Map<String, Object>> searchWithElasticsearch(String keyword, String type, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<QuestionDocument> page;
        
        if (keyword == null || keyword.trim().isEmpty()) {
            page = questionRepository.findAll(pageable);
        } else if (type != null && !type.isEmpty()) {
            page = questionRepository.searchByKeywordAndType(keyword, type, pageable);
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
                                                   @RequestParam(required = false) String type,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "20") Integer pageSize,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return searchQuestion(keyword, type, pageNum, pageSize, userId);
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
