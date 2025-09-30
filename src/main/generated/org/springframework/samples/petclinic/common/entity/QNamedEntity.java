package org.springframework.samples.petclinic.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNamedEntity is a Querydsl query type for NamedEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QNamedEntity extends EntityPathBase<NamedEntity> {

    private static final long serialVersionUID = 695163905L;

    public static final QNamedEntity namedEntity = new QNamedEntity("namedEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath name = createString("name");

    public QNamedEntity(String variable) {
        super(NamedEntity.class, forVariable(variable));
    }

    public QNamedEntity(Path<? extends NamedEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNamedEntity(PathMetadata metadata) {
        super(NamedEntity.class, metadata);
    }

}

