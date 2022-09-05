package com.example.intermediate.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.intermediate.controller.request.PostRequestDto;
import com.example.intermediate.controller.response.CommentResponseDto;
import com.example.intermediate.controller.response.PostResponseAllDto;
import com.example.intermediate.controller.response.PostResponseDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.domain.Comment;
import com.example.intermediate.domain.Member;
import com.example.intermediate.domain.Post;
import com.example.intermediate.external.UploadService;
import com.example.intermediate.jwt.TokenProvider;
import com.example.intermediate.repository.CommentRepository;
import com.example.intermediate.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;


@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;

  private final TokenProvider tokenProvider;
  private final static Logger LOG = Logger.getGlobal();
  private final UploadService s3Service;
  @Transactional
  public ResponseDto<?> createPost(PostRequestDto requestDto1,PostRequestDto requestDto2, HttpServletRequest request,MultipartFile file) {   //
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

    String fileName = createFileName(file.getOriginalFilename());  // 파일 이름을 유니크한 이름으로 재지정. 같은 이름의 파일을 업로드 하면 overwrite 됨
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(file.getContentType());
    objectMetadata.setContentLength(file.getSize());
    try (InputStream inputStream = file.getInputStream()) {
      s3Service.uploadFile(inputStream, objectMetadata, fileName);
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("파일 변환 중 에러가 발생하였습니다 (%s)", file.getOriginalFilename()));
    }
     ResponseDto.success(s3Service.getFileUrl(fileName));



    Post post = Post.builder()
        .title(requestDto1.getTitle())
        .content(requestDto2.getContent())
        .imgUrl(s3Service.getFileUrl(fileName))
        .likes(0)
        .member(member)
        .build();
    postRepository.save(post);
    return ResponseDto.success(
        PostResponseDto.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .imgUrl(post.getImgUrl())
            .likes(post.getLikes())
            .nickname(post.getMember().getNickname())
            .createdAt(post.getCreatedAt())
            .modifiedAt(post.getModifiedAt())
            .build()
    );
  }

  @Transactional(readOnly = true)
  public ResponseDto<?> getPost(Long id) {
    Post post = isPresentPost(id);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    List<Comment> commentList = commentRepository.findAllByPost(post);
    List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();



    for (Comment comment : commentList) {
      commentResponseDtoList.add(
          CommentResponseDto.builder()
              .id(comment.getId())
              .nickname(comment.getMember().getNickname())
              .content(comment.getContent())
              .createdAt(comment.getCreatedAt())
              .modifiedAt(comment.getModifiedAt())
              .build()
      );
    }

    return ResponseDto.success(
        PostResponseDto.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .imgUrl(post.getImgUrl())
            .likes(post.getLikes())
            .commentResponseDtoList(commentResponseDtoList)
            .nickname(post.getMember().getNickname())
            .createdAt(post.getCreatedAt())
            .modifiedAt(post.getModifiedAt())
            .build()
    );
  }

  @Transactional(readOnly = true)
  public ResponseDto<?> getAllPost() {
    List<Post> postList = postRepository.findAllByOrderByModifiedAtDesc();
    List<PostResponseAllDto> postResponseAllDto = new ArrayList<>();
    for (Post post : postList) {
      int numberOfComment = commentRepository.findAllByPost(post).size();
      postResponseAllDto.add(
              PostResponseAllDto.builder()
                      .id(post.getId())
                      .title(post.getTitle())
                      .comments(numberOfComment)
                      .imgUrl(post.getImgUrl())
                      .likes(post.getLikes())
                      .nickname(post.getMember().getNickname())
                      .createdAt(post.getCreatedAt())
                      .modifiedAt(post.getModifiedAt())
                      .build()
      );
    }
    return ResponseDto.success(postResponseAllDto);
    // original code
//    return ResponseDto.success(postRepository.findAllByOrderByModifiedAtDesc());
  }

  @Transactional
  public ResponseDto<Post> updatePost(Long id, PostRequestDto requestDto, HttpServletRequest request) {
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

    Post post = isPresentPost(id);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    if (post.validateMember(member)) {
      return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
    }

    post.update(requestDto);
    return ResponseDto.success(post);
  }

  @Transactional
  public ResponseDto<?> deletePost(Long id, HttpServletRequest request) {
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

    Post post = isPresentPost(id);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    if (post.validateMember(member)) {
      return ResponseDto.fail("BAD_REQUEST", "작성자만 삭제할 수 있습니다.");
    }

    postRepository.delete(post);
    return ResponseDto.success("delete success");
  }



  @Transactional(readOnly = true)
  public Post isPresentPost(Long id) {
    Optional<Post> optionalPost = postRepository.findById(id);
    return optionalPost.orElse(null);
  }

  @Transactional
  public Member validateMember(HttpServletRequest request) {
    if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
      return null;
    }
    return tokenProvider.getMemberFromAuthentication();
  }

  // 스케쥴러에 쓰이는 게시글 정리하는 메소드
  @Transactional
  public void organize(Long id) {
    Post post = postRepository.findById(id).get();
    if (post.getComments().isEmpty()) {
      postRepository.deleteById(id);
      LOG.info("게시물<"+post.getTitle()+">이 삭제되었습니다");
    }
  }

  private String createFileName(String originalFileName) {
    return UUID.randomUUID().toString().concat(getFileExtension(originalFileName));
  }

  private String getFileExtension(String fileName) {
    try {
      return fileName.substring(fileName.lastIndexOf("."));
    } catch (StringIndexOutOfBoundsException e) {
      throw new IllegalArgumentException(String.format("잘못된 형식의 파일 (%s) 입니다", fileName));
    }
  }

}
