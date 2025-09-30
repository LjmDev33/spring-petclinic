package org.springframework.samples.petclinic.community;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCommunityPost is a Querydsl query type for CommunityPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCommunityPost extends EntityPathBase<CommunityPost> {

    private static final long serialVersionUID = -762475951L;

    public static final QCommunityPost communityPost = new QCommunityPost("communityPost");

    public final org.springframework.samples.petclinic.common.entity.QBaseEntity _super = new org.springframework.samples.petclinic.common.entity.QBaseEntity(this);

    public final StringPath author = createString("author");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final StringPath title = createString("title");

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public QCommunityPost(String variable) {
        super(CommunityPost.class, forVariable(variable));
    }

    public QCommunityPost(Path<? extends CommunityPost> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCommunityPost(PathMetadata metadata) {
        super(CommunityPost.class, metadata);
    }

}

