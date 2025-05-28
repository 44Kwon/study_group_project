package com.group_platform.post.service;

import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.post.entity.Post;
import com.group_platform.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Post validatePostWithPostId(Long postId) {
        return postRepository.findById(postId).orElseThrow(()-> new BusinessLogicException(ExceptionCode.POST_NOT_EXIST));
    }
}
