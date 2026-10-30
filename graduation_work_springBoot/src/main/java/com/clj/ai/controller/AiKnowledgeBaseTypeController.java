package com.clj.ai.controller;

import com.clj.ai.domain.AiKnowledgeBaseType;
import com.clj.ai.dto.AiKnowledgeBaseTypeDto;
import com.clj.ai.service.AiKnowledgeBaseTypeService;
import com.clj.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/knowledgeBaseType")
@RequiredArgsConstructor
public class AiKnowledgeBaseTypeController {
    private final AiKnowledgeBaseTypeService typeService;

    @GetMapping("/list")
    public Result<List<AiKnowledgeBaseType>> list() {
        return Result.success(typeService.listEnabled());
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody(required = false) AiKnowledgeBaseTypeDto dto) {
        typeService.saveType(dto);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody(required = false) AiKnowledgeBaseTypeDto dto) {
        typeService.updateType(dto);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        typeService.deleteType(id);
        return Result.success();
    }
}
