package com.poti.search.repository;

import com.poti.search.entity.QuestionDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface QuestionRepository extends ElasticsearchRepository<QuestionDocument, Long> {

    @Query("{\"bool\": {\"should\": [{\"match\": {\"title\": {\"query\": \"?0\", \"boost\": 2.0}}}, {\"match\": {\"content\": \"?0\"}}, {\"match\": {\"analysis\": \"?0\"}}, {\"match\": {\"tags\": \"?0\"}}, {\"match\": {\"categoryName.seg\": \"?0\"}}]}}")
    Page<QuestionDocument> searchByKeyword(String keyword, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"bool\": {\"should\": [{\"match\": {\"title\": {\"query\": \"?0\", \"boost\": 2.0}}}, {\"match\": {\"content\": \"?0\"}}, {\"match\": {\"analysis\": \"?0\"}}, {\"match\": {\"tags\": \"?0\"}}, {\"match\": {\"categoryName.seg\": \"?0\"}}]}}, {\"term\": {\"categoryId\": \"?1\"}}]}}")
    Page<QuestionDocument> searchByKeywordAndCategoryId(String keyword, Long categoryId, Pageable pageable);

    List<QuestionDocument> findByType(String type);

    List<QuestionDocument> findByCategoryId(Long categoryId);

    /**
     * 搜索联想：题干/标签/分类名前缀匹配（支持输入未打完的词，如 "Jav" 联想出 "Java"）
     */
    @Query("{\"bool\": {\"should\": [{\"match_phrase_prefix\": {\"title\": {\"query\": \"?0\", \"boost\": 2.0}}}, {\"match_phrase_prefix\": {\"tags\": {\"query\": \"?0\"}}}, {\"match_phrase_prefix\": {\"categoryName.seg\": {\"query\": \"?0\"}}}, {\"match\": {\"tags\": {\"query\": \"?0\"}}}, {\"match\": {\"categoryName.seg\": {\"query\": \"?0\"}}}]}}")
    Page<QuestionDocument> suggestByKeyword(String keyword, Pageable pageable);
}
