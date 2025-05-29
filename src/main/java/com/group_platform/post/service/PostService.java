package com.group_platform.post.service;

import com.group_platform.comment.dto.CommentDto;
import com.group_platform.comment.repository.CommentRepository;
import com.group_platform.comment.service.CommentService;
import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.post.dto.PostDto;
import com.group_platform.post.entity.Post;
import com.group_platform.post.entity.PostType;
import com.group_platform.post.mapper.PostMapper;
import com.group_platform.post.repository.PostRepository;
import com.group_platform.response.PageInfo;
import com.group_platform.studymember.entity.StudyMember;
import com.group_platform.studymember.repository.StudyMemberRepository;
import com.group_platform.studymember.service.StudyMemberService;
import com.group_platform.sutdygroup.service.StudyGroupService;
import com.group_platform.user.entity.User;
import com.group_platform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserService userService;
    private final StudyMemberService studyMemberService;
    private final StudyMemberRepository studyMemberRepository;
    private final CommentService commentService;
    private final CommentRepository commentRepository;

    public Long crateCommonPost(Long userId, PostDto.CreateRequestForCommonPost postDto) {
        //연관관계용 user검증
        User user = userService.validateUserWithUserId(userId);

        Post commonToPost = postMapper.createCommonToPost(postDto);
        //NOTICE("공지"),INTRODUCTION("자기소개") 튕기게
        if (commonToPost.getPostType() == PostType.NOTICE || commonToPost.getPostType() == PostType.INTRODUCTION) {
            throw new BusinessLogicException(ExceptionCode.INVALID_POST_TYPE);
        }

        commonToPost.addCommonPost(user);
        Post savedPost = postRepository.save(commonToPost);
        return savedPost.getId();
    }

    public Long createGroupPost(Long userId, PostDto.CreateRequestForGroupPost postDto, Long studyGroupId) {
        Post groupToPost = postMapper.createGroupToPost(postDto);
        //RECRUITMENT("모집"),  //모집은 공통 게시판에서만(그룹 내 게시판에서는 튕기게)
        if (groupToPost.getPostType() == PostType.RECRUITMENT) {
            throw new BusinessLogicException(ExceptionCode.INVALID_POST_TYPE);
        }
        //Fetch join으로 한번에 가져옴
        //유저가 해당 그룹인원인지 검증
        StudyMember studyMember = studyMemberService.validateMemberWithUserAndGroup(userId, studyGroupId,StudyMember.ActiveStatus.ACTIVE);

        //NOTICE("공지"),   //공지사항은 리더만 쓸수있게
        if (groupToPost.getPostType() == PostType.NOTICE && studyMember.getRole() != StudyMember.InGroupRole.LEADER) {
            throw new BusinessLogicException(ExceptionCode.INVALID_POST_TYPE);
        }

        //연관관계 시 스터디그룹과 유저 둘다
        groupToPost.addGroupPost(studyMember.getUser(), studyMember.getStudyGroup());

        Post savedPost = postRepository.save(groupToPost);
        return savedPost.getId();
    }

    public PostDto.Response updatePost(Long userId, PostDto.UpdateRequest updateRequest) {
        Post updatePost = postMapper.updateToPost(updateRequest);

        Post post = validatePost(updatePost.getId());
        //내가 쓴 post인지
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.NO_PERMISSION);
        }

        //groupid가 있냐 없냐에 따라 다른 postType 검증 처리
        validatePostType(userId, updateRequest, post);

        Optional.ofNullable(updatePost.getTitle())
                .ifPresent(post::changeTitle);
        Optional.ofNullable(updatePost.getContent())
                .ifPresent(post::changeContent);
        Optional.ofNullable(updateRequest.getPostType())
                .ifPresent(post::changePostType);

        PostDto.Response response = postMapper.PostToResponse(post);
        // response에 댓글 세팅(마지막 페이징로(최신오름차순), 대댓글 몇개인지까지 표기된 service 것 사용)
        settingCommentsForPost(response);
        return response;
    }

//    @Transactional(readOnly = true) - 조회수 update중
    public PostDto.Response getPost(Long postId) {
        Post post = validatePost(postId);
        //조회수 증가처리
        //동시성 문제 방지차원
        postRepository.incrementViewCount(postId);
        PostDto.Response response = postMapper.PostToResponse(post);
        // response에 댓글 세팅
        settingCommentsForPost(response);
        return response;
    }

    public void deletePost(Long userId, Long postId) {
        Post post = validatePost(postId);
        //향후 변경 로직 변경
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.NO_PERMISSION);
        }
        postRepository.delete(post);
    }

    private Post validatePost(Long postId) {
        return postRepository.findById(postId).orElseThrow(()-> new BusinessLogicException(ExceptionCode.POST_NOT_EXIST));
    }

    // postType 검증 처리(공통 vs 그룹 내 게시글)
    private void validatePostType(Long userId, PostDto.UpdateRequest updateRequest, Post post) {
        if (post.getStudyGroup() == null && updateRequest.getPostType() != null) {
            if (updateRequest.getPostType() == PostType.NOTICE || updateRequest.getPostType() == PostType.INTRODUCTION) {
                throw new BusinessLogicException(ExceptionCode.INVALID_POST_TYPE);
            }
        } else if (post.getStudyGroup() != null && updateRequest.getPostType() != null) {
            if (updateRequest.getPostType() == PostType.RECRUITMENT) {
                throw new BusinessLogicException(ExceptionCode.INVALID_POST_TYPE);
            }
            //리더인지
            boolean isLeader = studyMemberRepository.existsByUserIdAndStatusAndRole(userId, StudyMember.ActiveStatus.ACTIVE, StudyMember.InGroupRole.LEADER);
            if (updateRequest.getPostType() == PostType.NOTICE && !isLeader) {
                throw new BusinessLogicException(ExceptionCode.INVALID_POST_TYPE);
            }
        }
    }

    //게시글에 딸린 댓글 세팅(최신페이지, 즉 마지막 페이지로 가게끔 한다) -> 현재 commentService 사용중
    private void settingCommentsForPost(PostDto.Response response) {
        long totalCount = commentRepository.countAllByPostId(response.getId());
        int lastPage = (totalCount == 0) ? 0 : (int)((totalCount - 1) / 10);    //마지막 페이지(댓글 최신)
        Pageable pageable = PageRequest.of(lastPage, 10, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<CommentDto.ResponseCommentList> commentListPage = commentService.getAllComments(response.getId(), pageable);
        response.setComments(commentListPage.getContent());
        response.setCommentsPage(
                new PageInfo(commentListPage.getNumber()+1, commentListPage.getSize(), commentListPage.getTotalElements(), commentListPage.getTotalPages()));
    }
}
