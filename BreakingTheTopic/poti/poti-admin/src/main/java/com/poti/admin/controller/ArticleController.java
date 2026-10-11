package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.Article;
import com.poti.admin.mapper.ArticleMapper;
import com.poti.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 文章管理接口（后台）。
 */
@RestController
@RequestMapping("/admin/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleMapper articleMapper;

    /** 分页列表（status：0草稿 1已发布 2下架，不传查全部） */
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(Article::getCategory, category.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword.trim())
                    .or().like(Article::getTags, keyword.trim()));
        }
        wrapper.orderByDesc(Article::getPinned).orderByDesc(Article::getId);

        Page<Article> page = articleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        return R.success(data);
    }

    /** 详情 */
    @GetMapping("/{id}")
    public R<Article> detail(@PathVariable Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return R.error("文章不存在或已被删除");
        }
        return R.success(article);
    }

    /** 新增文章 */
    @PostMapping
    public R<Void> create(@RequestBody Map<String, Object> body) {
        Article article = parse(body, new Article());
        if (article.getTitle() == null || article.getTitle().isBlank()) {
            return R.error("文章标题不能为空");
        }
        if (article.getContent() == null || article.getContent().isBlank()) {
            return R.error("文章内容不能为空");
        }
        article.setId(null);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        if (article.getStatus() != null && article.getStatus() == 1) {
            article.setPublishTime(LocalDateTime.now());
        }
        articleMapper.insert(article);
        return R.success();
    }

    /** 编辑文章 */
    @PutMapping
    public R<Void> update(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") == null ? null : Long.valueOf(String.valueOf(body.get("id")));
        if (id == null) {
            return R.error("缺少文章 ID");
        }
        Article db = articleMapper.selectById(id);
        if (db == null) {
            return R.error("文章不存在或已被删除");
        }
        Article article = parse(body, db);
        // 首次发布时记录发布时间
        if (article.getStatus() != null && article.getStatus() == 1 && db.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        article.setId(id);
        articleMapper.updateById(article);
        return R.success();
    }

    /** 修改发布状态（发布 / 下架） */
    @PutMapping("/status/{id}")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            return R.error("状态非法（0草稿 1已发布 2下架）");
        }
        Article db = articleMapper.selectById(id);
        if (db == null) {
            return R.error("文章不存在或已被删除");
        }
        db.setStatus(status);
        if (status == 1 && db.getPublishTime() == null) {
            db.setPublishTime(LocalDateTime.now());
        }
        articleMapper.updateById(db);
        return R.success();
    }

    /** 删除文章 */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Article db = articleMapper.selectById(id);
        if (db == null) {
            return R.error("文章不存在或已被删除");
        }
        articleMapper.deleteById(id);
        return R.success();
    }

    private Article parse(Map<String, Object> body, Article target) {
        if (body.containsKey("title")) {
            target.setTitle(str(body.get("title")));
        }
        if (body.containsKey("category")) {
            target.setCategory(str(body.get("category")));
        }
        if (body.containsKey("cover")) {
            target.setCover(str(body.get("cover")));
        }
        if (body.containsKey("tags")) {
            target.setTags(str(body.get("tags")));
        }
        if (body.containsKey("summary")) {
            target.setSummary(str(body.get("summary")));
        }
        if (body.containsKey("content")) {
            target.setContent(String.valueOf(body.get("content")));
        }
        if (body.containsKey("authorName")) {
            target.setAuthorName(str(body.get("authorName")));
        }
        if (body.containsKey("pinned")) {
            target.setPinned(intVal(body.get("pinned"), 0));
        }
        if (body.containsKey("recommended")) {
            target.setRecommended(intVal(body.get("recommended"), 0));
        }
        if (body.containsKey("status")) {
            target.setStatus(intVal(body.get("status"), 0));
        }
        return target;
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private Integer intVal(Object o, int def) {
        try {
            return o == null ? def : Integer.valueOf(String.valueOf(o));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
