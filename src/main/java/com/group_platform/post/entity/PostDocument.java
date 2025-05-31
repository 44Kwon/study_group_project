package com.group_platform.post.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "posts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private String id;
    @Field(type = FieldType.Text)
    private String title;
    @Field(type = FieldType.Text)
    private String content;
    private LocalDateTime createdAt;
    @Field(type = FieldType.Keyword)
    private String groupId;

    public static PostDocument from(Post post) {
        return new PostDocument(
                post.getId().toString(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getStudyGroup() != null ? String.valueOf(post.getStudyGroup().getId()) : null
        );
    }
}
