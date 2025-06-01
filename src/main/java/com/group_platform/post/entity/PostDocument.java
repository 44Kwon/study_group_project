package com.group_platform.post.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Document(indexName = "posts", createIndex = true)
@Setting(settingPath = "elasticsearch/settings.json")  //인덱스 설정 (settings), 분석기 설정
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) //_class필드 필요없음
public class PostDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private String id;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "korean_analyzer", searchAnalyzer = "korean_analyzer"),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer", searchAnalyzer = "ngram_analyzer")
            }
    )
    private String title;
    @Field(type = FieldType.Text, analyzer = "korean_analyzer", searchAnalyzer = "korean_analyzer")
    private String content;
    @Field(type = FieldType.Keyword)
    private String groupId;

//    @Field(type = FieldType.Date, format = DateFormat.date_time)
//    private LocalDateTime createdAt;

    public static PostDocument from(Post post) {
        return new PostDocument(
                post.getId().toString(),
                post.getTitle(),
                post.getContent(),
//                post.getCreatedAt(),
                post.getStudyGroup() != null ? String.valueOf(post.getStudyGroup().getId()) : null
        );
    }
}
