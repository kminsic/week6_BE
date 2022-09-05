package com.example.intermediate.controller;

import com.example.intermediate.controller.request.PostRequestDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.service.FileUploadService;
import com.example.intermediate.service.PostService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class PostController {

  private final PostService postService;


  // 게시글 작성
  @RequestMapping(value = "/api/auth/posts", method = RequestMethod.POST)
  public ResponseDto<?> createPost(@RequestPart(value = "content") PostRequestDto requestDto,
      HttpServletRequest request,@RequestPart(value = "image") MultipartFile file) {
    return postService.createPost(requestDto, request,file);
  }

  //이미지 업로드
//  @RequestMapping(value = "/api/auth/posts", method = RequestMethod.POST)
//  public ResponseDto<?> uploadImage(@RequestPart MultipartFile file) {
//    return fileUploadService.uploadImage(file);
//  }


  // 상세 게시글 가져오기
  @RequestMapping(value = "/api/posts/{id}", method = RequestMethod.GET)
  public ResponseDto<?> getPost(@PathVariable Long id) {
    return postService.getPost(id);
  }

  // 전체 게시글 가져오기
  @RequestMapping(value = "/api/posts", method = RequestMethod.GET)
  public ResponseDto<?> getAllPosts() {
    return postService.getAllPost();
  }

  // 게시글 수정
  @RequestMapping(value = "/api/auth/posts/{id}", method = RequestMethod.PUT)
  public ResponseDto<?> updatePost(@PathVariable Long id, @RequestBody PostRequestDto postRequestDto,
      HttpServletRequest request) {
    return postService.updatePost(id, postRequestDto, request);
  }

  //게시글 삭제
  @RequestMapping(value = "/api/auth/posts/{id}", method = RequestMethod.DELETE)
  public ResponseDto<?> deletePost(@PathVariable Long id,
      HttpServletRequest request) {
    return postService.deletePost(id, request);
  }

  // 테스트
  @RequestMapping(value = "/api/posts/organize/{id}", method = RequestMethod.DELETE)
  public String organizePost(@PathVariable Long id) {
    postService.organize(id);
    return "scheduler completed";
  }
}
