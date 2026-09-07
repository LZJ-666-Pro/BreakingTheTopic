package com.poti.search.repository;

import com.poti.search.entity.QuestionDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface QuestionRepository extends ElasticsearchRepository<QuestionDocument, Long> {

    @Query("{\"bool\": {\"should\": [{\"match\": {\"title\": \"?0\"}}, {\"match\": {\"content\": \"?0\"}}, {\"match\": {\"analysis\": \"?0\"}}, {\"match\": {\"tags\": \"?0\"}}]}}")
    Page<QuestionDocument> searchByKeyword(String keyword, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"bool\": {\"should\": [{\"match\": {\"title\": \"?0\"}}, {\"match\": {\"content\": \"?0\"}}, {\"match\": {\"analysis\": \"?0\"}}, {\"match\": {\"tags\": \"?0\"}}]}}, {\"term\": {\"type\": \"?1\"}}]}}")
    Page<QuestionDocument> searchByKeywordAndType(String keyword, String type, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"bool\": {\"should\": [{\"match\": {\"title\": \"?0\"}}, {\"match\": {\"content\": \"?0\"}}, {\"match\": {\"analysis\": \"?0\"}}, {\"match\": {\"tags\": \"?0\"}}]}}, {\"term\": {\"categoryId\": \"?1\"}}]}}")
    Page<QuestionDocument> searchByKeywordAndCategoryId(String keyword, Long categoryId, Pageable pageable);

    List<QuestionDocument> findByType(String type);

    List<QuestionDocument> findByCategoryId(Long categoryId);
}
