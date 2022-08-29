package com.example.intermediate.controller;

import com.example.intermediate.controller.request.CommentRequestDto;
import com.example.intermediate.controller.request.SubCommentRequestDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.service.SubCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Validated
@RequiredArgsConstructor
@RestController
public class SubCommentController {

    private final SubCommentService subCommentService;


    @RequestMapping(value = "/api/auth/subComment", method = RequestMethod.POST)
    public ResponseDto<?> createSubComment(@RequestBody SubCommentRequestDto requestDto,
                                           HttpServletRequest request) {
        return subCommentService.createSubComment(requestDto, request);
    }

    @RequestMapping(value = "/api/subComment/{id}", method = RequestMethod.GET)
    public ResponseDto<?> getAllSubComments(@PathVariable Long id) {
        return subCommentService.getAllSubCommentsByComment(id);
    }

    @RequestMapping(value = "/api/auth/subComment/{id}", method = RequestMethod.PUT)
    public ResponseDto<?> updateSubComment(@PathVariable Long id, @RequestBody SubCommentRequestDto requestDto,
                                        HttpServletRequest request) {
        return subCommentService.updateSubComment(id, requestDto, request);
    }

    @RequestMapping(value = "/api/auth/subComment/{id}", method = RequestMethod.DELETE)
    public ResponseDto<?> deleteComment(@PathVariable Long id,
                                        HttpServletRequest request) {
        return subCommentService.deleteSubComment(id, request);
    }

    @RequestMapping(value = "/api/auth/subComment/like/{id}", method = RequestMethod.GET)
    public ResponseDto<?> addLike(@PathVariable Long id, HttpServletRequest request) {
        return subCommentService.addLike(id, request);
    }
}
