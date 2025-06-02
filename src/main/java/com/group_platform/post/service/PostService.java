package com.group_platform.post.service;

import com.group_platform.comment.dto.CommentDto;
import com.group_platform.comment.repository.CommentRepository;
import com.group_platform.comment.service.CommentService;
import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.post.bookmark.PostBookmark;
import com.group_platform.post.dto.*;
import com.group_platform.post.entity.Post;
import com.group_platform.post.entity.PostType;
import com.group_platform.post.event.PostCreatedEvent;
import com.group_platform.post.event.PostDeletedEvent;
import com.group_platform.post.event.PostUpdatedEvent;
import com.group_platform.post.like.PostLike;
import com.group_platform.post.mapper.PostMapper;
import com.group_platform.post.repository.PostBookmarkRepository;
import com.group_platform.post.repository.PostLikeRepository;
import com.group_platform.post.repository.PostRepository;
import com.group_platform.post.repository.elasticsearch.PostSearchRepository;
import com.group_platform.response.PageInfo;
import com.group_platform.studymember.entity.StudyMember;
import com.group_platform.studymember.repository.StudyMemberRepository;
import com.group_platform.studymember.service.StudyMemberService;
import com.group_platform.user.entity.User;
import com.group_platform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final ApplicationEventPublisher eventPublisher;
    private final PostSearchRepository postSearchRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostBookmarkRepository postBookmarkRepository;

    public Long crateCommonPost(Long userId, PostDto.CreateRequestForCommonPost postDto) {
        //연관관계용 user검증
        User user = userService.validateUserWithUserId(userId);

        //NOTICE("공지"),INTRODUCTION("자기소개") 튕기게
        if (postDto.getPostType() == PostType.NOTICE || postDto.getPostType() == PostType.INTRODUCTION) {
            throw new BusinessLogicException(ExceptionCode.INVALID_POST_TYPE);
        }

        Post commonToPost = postMapper.createCommonToPost(postDto);
        if(commonToPost.getPostType() == null) {
            //매퍼사용에서 빌더디폴트가 안먹힘...
            commonToPost.setPostType(PostType.GENERAL);
        }

        commonToPost.addCommonPost(user);
        Post savedPost = postRepository.save(commonToPost);
        //엘라스틱 서치에 저장
        eventPublisher.publishEvent(new PostCreatedEvent(savedPost));
        return savedPost.getId();
    }

    public Long createGroupPost(Long userId, PostDto.CreateRequestForGroupPost postDto, Long studyGroupId) {
        Post groupToPost = postMapper.createGroupToPost(postDto);
        if(groupToPost.getPostType() == null) {
            groupToPost.setPostType(PostType.GENERAL);
        }
        //RECRUITMENT("모집"),  //모집은 공통 게시판에서만(그룹 내 게시판에서는 튕기게)
        if (groupToPost.getPostType() == PostType.RECRUITMENT) {
            throw new BusinessLogicException(ExceptionCode.INVALID_POST_TYPE);
        }
        //Fetch join으로 한번에 가져옴
        //유저가 해당 그룹인원인지 검증
        StudyMember studyMember = studyMemberService.validateMemberWithUserAndGroup(userId, studyGroupId,StudyMember.ActiveStatus.ACTIVE);
        if (studyMember.getRole() != StudyMember.InGroupRole.LEADER && groupToPost.isPinned()) {
            // 리더가 아니면 pinned 불가
            throw new BusinessLogicException(ExceptionCode.PINNED_VALID_ONLY_LEADER);
        }

        //NOTICE("공지"),   //공지사항은 리더만 쓸수있게
        if (groupToPost.getPostType() == PostType.NOTICE && studyMember.getRole() != StudyMember.InGroupRole.LEADER) {
            throw new BusinessLogicException(ExceptionCode.INVALID_POST_TYPE);
        }

        //연관관계 시 스터디그룹과 유저 둘다
        groupToPost.addGroupPost(studyMember.getUser(), studyMember.getStudyGroup());

        Post savedPost = postRepository.save(groupToPost);
        //엘라스틱 서치에 저장
        eventPublisher.publishEvent(new PostCreatedEvent(savedPost));
        return savedPost.getId();
    }

    public PostResponseDto updatePost(Long userId, PostDto.UpdateRequest updateRequest) {
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

        //엘라스틱 서치에 업데이트
        eventPublisher.publishEvent(new PostUpdatedEvent(post));

        PostResponseDto response = postMapper.PostToResponse(post);
        // response에 댓글 세팅(마지막 페이징로(최신오름차순), 대댓글 몇개인지까지 표기된 service 것 사용)
        settingCommentsForPost(response,userId);
        response.setMine(true);
        return response;
    }

//    @Transactional(readOnly = true) - 조회수 update중
    // 공통게시글 조회
    public PostResponseDto getCommonPost(Long userId, Long postId) {
        Post post = validatePost(postId);
        //잘못된 경로로 접근 차단(그룹게시글일때 튕기기)
        if(post.getStudyGroup() != null) {
            throw new BusinessLogicException(ExceptionCode.INVALID_BOARD_ACCESS);
        }
        return getResponseDto(userId, post);
    }

    // 그룹게시글 조회
    public PostResponseDto getGroupPost(Long userId, Long groupId, Long postId) {
        Post post = validatePost(postId);
        //잘못된 경로로 접근 차단(공통게시글일때 튕기기)
        if(post.getStudyGroup() == null) {
            throw new BusinessLogicException(ExceptionCode.INVALID_BOARD_ACCESS);
        }

        //유저가 해당 스터디그룹 멤버인지
        boolean isMember = studyMemberRepository.existsByUser_IdAndStudyGroup_IdAndStatus(userId, groupId, StudyMember.ActiveStatus.ACTIVE);

        //그룹원 아니면 튕기기
        if (!isMember) {
            throw new BusinessLogicException(ExceptionCode.NO_PERMISSION);
        }
        return getResponseDto(userId, post);
    }

    // 게시글 상세조회 시 (게시글 검증 + 조회수 증가 + responseDto 세팅)
    private PostResponseDto getResponseDto(Long userId, Post post) {
        //조회수 증가처리
        //동시성 문제 방지차원
        postRepository.incrementViewCount(post.getId());
        PostResponseDto response = postMapper.PostToResponse(post);
        // response에 댓글 세팅
        settingCommentsForPost(response, userId);
        //내가쓴건지
        if(response.getWriter().getId().equals(userId)) {
            response.setMine(true);
        }
        //좋아요 헀는지, 찜 했는지
        if (postLikeRepository.existsByUserIdAndPostId(userId, response.getId())) {
            response.setLiked(true);
        }
        if (postBookmarkRepository.existsByUserIdAndPostId(userId, response.getId())) {
            response.setBookmarked(true);
        }

        return response;
    }

    //게시글 삭제
    public void deletePost(Long userId, Long postId) {
        Post post = validatePost(postId);
        //향후 변경 로직 변경
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.NO_PERMISSION);
        }
        postRepository.delete(post);
        //엘라스틱서치에서 삭제
        eventPublisher.publishEvent(new PostDeletedEvent(postId));
    }

    //공통게시글 조회
    @Transactional(readOnly = true)
    public Page<PostResponseListDto> getCommonPosts(Long userId, PostType type, PostSortType sort, Pageable pageable) {
        //PostType이 공통에 맞지 않은거면 튕기기
        //NOTICE("공지"),INTRODUCTION("자기소개") 튕기게
        if (type == PostType.NOTICE || type == PostType.INTRODUCTION) {
            throw new BusinessLogicException(ExceptionCode.INVALID_POST_TYPE);
        }

        //userId는 내것인지 확인용으로 쓸것
        return postRepository.getCommonPosts(userId, type, sort, pageable);
    }

    // 상단고정 고려할 것
    // 1순위 : 공지사항 + 고정
    // 2순위 : 고정
    // 일반 공지사항은 그냥 프론트에서 빨간색으로 처리한다던지...
    @Transactional(readOnly = true)
    public PostListResponse getGroupPosts(Long userId, Long groupId, PostType type, PostSortType sort, Pageable pageable) {
        //유저가 해당 스터디그룹 멤버인지
        boolean isMember = studyMemberRepository.existsByUser_IdAndStudyGroup_IdAndStatus(userId, groupId, StudyMember.ActiveStatus.ACTIVE);

        //그룹원 아니면 튕기기
        if (!isMember) {
            throw new BusinessLogicException(ExceptionCode.NO_PERMISSION);
        }
        //게시글 타입(공통용은 튕기기)
        if (type == PostType.RECRUITMENT) {
            throw new BusinessLogicException(ExceptionCode.INVALID_POST_TYPE);
        }

        //고정글조회
        List<PostResponseListDto> groupPinnedPosts = postRepository.getGroupPinnedPosts(userId, groupId);
        //목록조회
        Page<PostResponseListDto> groupNormalPosts = postRepository.getGroupNormalPosts(userId, groupId, type, sort, pageable);

        return new PostListResponse(groupPinnedPosts,groupNormalPosts);
    }

    //공통게시글 검색
    @Transactional(readOnly = true)
    public Page<PostResponseListDto> searchCommonPosts(Long userId, String keyword, Pageable pageable) {
        if(keyword == null || keyword.isEmpty()) {
            throw new BusinessLogicException(ExceptionCode.KEYWORD_MISSING);
        }
        //elasticsearch에서 id값만 가져오기
        Page<Long> searchCommonPosts = postSearchRepository.getSearchCommonPosts(keyword, pageable);

        List<Long> searchCommonPostsId = searchCommonPosts.getContent();
        List<PostResponseListDto> searchPosts = postRepository.getSearchPosts(userId, searchCommonPostsId, pageable);

        return new PageImpl<>(searchPosts,pageable,searchCommonPosts.getTotalElements());
    }



    //그룹게시글 검색
    @Transactional(readOnly = true)
    public PostListResponse searchGroupPosts(Long userId, Long groupId, String keyword, Pageable pageable) {
        if(keyword == null || keyword.isEmpty()) {
            throw new BusinessLogicException(ExceptionCode.KEYWORD_MISSING);
        }
        //유저가 해당 스터디그룹 멤버인지
        boolean isMember = studyMemberRepository.existsByUser_IdAndStudyGroup_IdAndStatus(userId, groupId, StudyMember.ActiveStatus.ACTIVE);
        //그룹원 아니면 튕기기
        if (!isMember) {
            throw new BusinessLogicException(ExceptionCode.NO_PERMISSION);
        }

        //고정글조회
        //향후 redis에 캐싱처리할것
        List<PostResponseListDto> groupPinnedPosts = postRepository.getGroupPinnedPosts(userId, groupId);

        //elasticsearch에서 id값만 가져오기
        Page<Long> searchGroupPosts = postSearchRepository.getSearchGroupPosts(groupId, keyword, pageable);
        List<Long> searchGroupPostsId = searchGroupPosts.getContent();
        List<PostResponseListDto> searchPosts = postRepository.getSearchPosts(userId, searchGroupPostsId, pageable);

        return new PostListResponse(groupPinnedPosts,new PageImpl<>(searchPosts,pageable,searchGroupPosts.getTotalElements()));
    }


    //상단고정
    public PostDto.UpdatePinnedResponse updatePinned(Long userId, Long groupId, Long postId) {
        //해당 그룹 리더만 가능하게
        boolean leader = studyMemberService.isLeader(userId, groupId);
        if(!leader) {
            throw new BusinessLogicException(ExceptionCode.PINNED_VALID_ONLY_LEADER);
        }
        //갯수가 10개이상이면 튕기게
        long count = postRepository.countByIsPinnedIsTrueAndStudyGroup_Id(groupId);
        if(count >= 10) {
            throw new BusinessLogicException(ExceptionCode.PINNED_NUM_IS_OVER);
        }

        //게시글이 그룹에 속한 글인지 검증할것
        Post post = validatePostWithPostIdAndPostId(groupId, postId);
        post.changePinned(true);
        return new PostDto.UpdatePinnedResponse(post.getId(), post.isPinned());
    }

    //상단고정 취소
    public PostDto.UpdatePinnedResponse updateUnPinned(Long userId, Long groupId, Long postId) {
        //해당 그룹 리더만 가능하게
        boolean leader = studyMemberService.isLeader(userId, groupId);
        if(!leader) {
            throw new BusinessLogicException(ExceptionCode.PINNED_VALID_ONLY_LEADER);
        }

        Post post = validatePostWithPostIdAndPostId(groupId, postId);
        post.changePinned(false);
        return new PostDto.UpdatePinnedResponse(post.getId(), post.isPinned());
    }

    //게시글 좋아요
    public PostDto.UpdateLikeOrFavouriteResponse likePost(Long userId, Long postId) {

        if (userId == null) {
            throw new BusinessLogicException(ExceptionCode.LIKE_AUTH_REQUIRED);
        }

        //유저가 존재하는지
        User user = userService.validateUserWithUserId(userId);

        //게시글이 존재하는지
        Post post = validatePost(postId);

        //좋아요를 했었는지
        boolean like = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(like) {
            throw new BusinessLogicException(ExceptionCode.LIKE_DUPLICATED);
        }

        PostLike postLike = new PostLike();
        postLike.likePost(post,user);
        postLikeRepository.save(postLike);
        //좋아요 수 증가
        postRepository.incrementLikeCount(postId);
        return new PostDto.UpdateLikeOrFavouriteResponse(postId, true);
    }


    //게시글 좋아요 취소
    public PostDto.UpdateLikeOrFavouriteResponse cancelLikePost(Long userId, Long postId) {
        if (userId == null) {
            throw new BusinessLogicException(ExceptionCode.LIKE_AUTH_REQUIRED);
        }

        boolean existsPost = postRepository.existsById(postId);
        if(!existsPost) {
            throw new BusinessLogicException(ExceptionCode.POST_NOT_EXIST);
        }

        boolean like = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(!like) {
            return new PostDto.UpdateLikeOrFavouriteResponse(postId, false);
        }
        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        //좋아요 수 마이너스
        postRepository.decrementLikeCount(postId);

        return new PostDto.UpdateLikeOrFavouriteResponse(postId, false);
    }


    //찜 등록
    public PostDto.UpdateLikeOrFavouriteResponse favoritePost(Long userId, Long postId) {
        if (userId == null) {
            throw new BusinessLogicException(ExceptionCode.FAVORITE_AUTH_REQUIRED);
        }

        //유저가 존재하는지
        User user = userService.validateUserWithUserId(userId);

        //게시글이 존재하는지
        Post post = validatePost(postId);

        //찜을 했었는지
        boolean favorite = postBookmarkRepository.existsByUserIdAndPostId(userId, postId);
        if(favorite) {
            throw new BusinessLogicException(ExceptionCode.FAVORITE_DUPLICATED);
        }

        PostBookmark postBookmark = new PostBookmark();
        postBookmark.bookmarkPost(post,user);
        postBookmarkRepository.save(postBookmark);

        return new PostDto.UpdateLikeOrFavouriteResponse(postId, true);
    }

    //찜 취소
    public PostDto.UpdateLikeOrFavouriteResponse cancelFavoritePost(Long userId, Long postId) {
        if (userId == null) {
            throw new BusinessLogicException(ExceptionCode.FAVORITE_AUTH_REQUIRED);
        }

        boolean existsPost = postRepository.existsById(postId);
        if(!existsPost) {
            throw new BusinessLogicException(ExceptionCode.POST_NOT_EXIST);
        }

        boolean favorite = postBookmarkRepository.existsByUserIdAndPostId(userId, postId);
        if(!favorite) {
            return new PostDto.UpdateLikeOrFavouriteResponse(postId, false);
        }
        postBookmarkRepository.deleteByUserIdAndPostId(userId, postId);

        return new PostDto.UpdateLikeOrFavouriteResponse(postId, false);
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
    private void settingCommentsForPost(PostResponseDto response, Long userId) {
        long totalCount = commentRepository.countAllByPostId(response.getId());
        int lastPage = (totalCount == 0) ? 0 : (int)((totalCount - 1) / 10);    //마지막 페이지(댓글 최신)
        Pageable pageable = PageRequest.of(lastPage, 10, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<CommentDto.ResponseCommentList> commentListPage = commentService.getAllComments(userId, response.getId(), pageable);
        response.setComments(commentListPage.getContent());
        response.setCommentsPage(
                new PageInfo(commentListPage.getNumber()+1, commentListPage.getSize(), commentListPage.getTotalElements(), commentListPage.getTotalPages()));
    }

    private Post validatePostWithPostIdAndPostId(Long groupId, Long postId) {
        return postRepository.findByIdAndStudyGroupId(postId, groupId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.POST_NOT_EXIST));
    }
}
