package org.springframework.samples.petclinic.faq.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.faq.service.FaqService;
import org.springframework.samples.petclinic.faq.table.FaqPost;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Project : spring-petclinic
 * File    : FaqController.java
 * Created : 2025-11-14
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: FAQ 게시판 화면 라우팅
 *   연관 기능:
 *     - FAQ 목록/상세 조회 화면 렌더링 (페이징, 검색, 필터링)
 *     - FAQ 등록/수정/삭제(관리자 전용)
 */
@Controller
@RequestMapping("/faq")
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    /**
     * FAQ 목록 조회 (페이징, 검색, 카테고리 필터)
     */
    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "type", defaultValue = "all") String type,
                       @RequestParam(value = "category", required = false) String category,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                       Model model) {

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<FaqPost> faqPage = faqService.searchFaqsWithPaging(keyword, category, type, pageable);

        model.addAttribute("faqs", faqPage.getContent());
        model.addAttribute("page", faqPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("category", category);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("template", "faq/faqList");
        return "fragments/layout";
    }

    /**
     * FAQ 상세 조회
     */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("faq", faqService.getFaq(id));
        model.addAttribute("template", "faq/faqDetail");
        return "fragments/layout";
    }
}

/**
 * FAQ 관리자 컨트롤러
 */
@Controller
@RequestMapping("/admin/faq")
class FaqAdminController {

    private final FaqService faqService;

    public FaqAdminController(FaqService faqService) {
        this.faqService = faqService;
    }

    /**
     * FAQ 등록 화면
     */
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("template", "faq/faqWrite");
        return "fragments/layout";
    }

    /**
     * FAQ 등록 처리
     */
    @PostMapping("/write")
    public String write(@RequestParam String question,
                        @RequestParam String answer,
                        @RequestParam String category,
                        @RequestParam(defaultValue = "0") Integer displayOrder) {
        faqService.createFaq(question, answer, category, displayOrder);
        return "redirect:/faq";
    }

    /**
     * FAQ 수정 화면
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("faq", faqService.getFaq(id));
        model.addAttribute("template", "faq/faqEdit");
        return "fragments/layout";
    }

    /**
     * FAQ 수정 처리
     */
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @RequestParam String question,
                       @RequestParam String answer,
                       @RequestParam String category,
                       @RequestParam(defaultValue = "0") Integer displayOrder) {
        faqService.updateFaq(id, question, answer, category, displayOrder);
        return "redirect:/faq/detail/" + id;
    }

    /**
     * FAQ 삭제 (Soft Delete) - 상세 페이지에서 단일 삭제
     */
    @PostMapping("/delete/{id}")
    public String deleteSingle(@PathVariable Long id) {
        faqService.softDeleteFaq(id);
        return "redirect:/faq";
    }

    /**
     * FAQ 삭제 (Soft Delete) - 목록에서 다중 삭제
     */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        ids.forEach(faqService::softDeleteFaq);
        return Map.of("success", true, "message", "FAQ가 삭제되었습니다.");
    }
}

