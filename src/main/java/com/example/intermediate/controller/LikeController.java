package com.example.intermediate.controller;

import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Validated
@RequiredArgsConstructor
@RestController
public class LikeController {
    private final LikeService likeService;

    // 게시글 좋아요
    @RequestMapping(value = "/api/auth/posts/like/{id}", method = RequestMethod.POST)
    public ResponseDto<?> addPostLike(@PathVariable Long id, HttpServletRequest request) {
        return likeService.addPostLike(id, request);
//        return likeService.addPostLike(id, request);
    }

    // 댓글 좋아요
    @RequestMapping(value = "/api/auth/comments/like/{id}", method = RequestMethod.POST)
    public ResponseDto<?> addCommentLike(@PathVariable Long id, HttpServletRequest request) {
        return likeService.addCommentLike(id, request);
    }

}
