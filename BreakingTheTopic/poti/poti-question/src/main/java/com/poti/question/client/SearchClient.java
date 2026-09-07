package com.poti.question.client;

import com.poti.question.dto.QuestionDocumentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "poti-search", path = "/search")
public interface SearchClient {

    @PostMapping("/sync")
    void syncQuestion(@RequestBody QuestionDocumentDTO questionDocument);

    @PostMapping("/sync/batch")
    void syncQuestions(@RequestBody List<QuestionDocumentDTO> questionDocuments);
}
