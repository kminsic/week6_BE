package com.example.intermediate.service;

import com.example.intermediate.controller.response.*;
import com.example.intermediate.domain.Comment;
import com.example.intermediate.domain.Member;
import com.example.intermediate.domain.Post;
import com.example.intermediate.jwt.TokenProvider;
import com.example.intermediate.repository.CommentRepository;
import com.example.intermediate.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final TokenProvider tokenProvider;


    public ResponseDto<?> getAllActs(HttpServletRequest request) {
        if (null == request.getHeader("RefreshToken")) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        if (null == request.getHeader("Authorization")) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        Member member = validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        //         Post 데이터 수집
        List<Post> postList = postRepository.findAllByMember(member);
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        for (Post post : postList) {
            postResponseDtoList.add(
                    PostResponseDto.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .imgUrl(post.getImgUrl())
                            .likes(post.getLikes())
                            .nickname(member.getNickname())
                            .createdAt(post.getCreatedAt())
                            .modifiedAt(post.getModifiedAt())
                            .build()
            );
        }

        // Comment 데이터 수집
        List<Comment> commentList = commentRepository.findAllByMember(member);
        List<CommentResponseMyPageDto> commentResponseMyPageDtoList = new ArrayList<>();
        for (Comment comment : commentList) {
            commentResponseMyPageDtoList.add(
                    CommentResponseMyPageDto.builder()
                            .id(comment.getId())
                            .nickname(member.getNickname())
                            .content(comment.getContent())
                            .likes(comment.getLikes())
                            .createdAt(comment.getCreatedAt())
                            .modifiedAt(comment.getModifiedAt())
                            .build()
            );
        }

        return ResponseDto.success(
                MyPageResponseDto.builder()
                        .postResponseDtoList(postResponseDtoList)
                        .commentResponseMyPageDtoList(commentResponseMyPageDtoList)
                        .build()
        );
    }

    @Transactional
    public Member validateMember(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication();
    }
}
