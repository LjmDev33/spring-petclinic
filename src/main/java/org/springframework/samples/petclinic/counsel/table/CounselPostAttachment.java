package org.springframework.samples.petclinic.counsel.table;

import jakarta.persistence.*;
import org.springframework.samples.petclinic.counsel.model.Attachment;

/**
 * @author Jeongmin Lee
 * @description 온라인 상담 게시글과 첨부파일의 관계를 정의하는 엔티티 클래스.
 */
@Entity
@Table(name = "counsel_post_attachments")
public class CounselPostAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counsel_post_id", nullable = false)
    private CounselPost counselPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_id", nullable = false)
    private Attachment attachment;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CounselPost getCounselPost() {
        return counselPost;
    }

    public void setCounselPost(CounselPost counselPost) {
        this.counselPost = counselPost;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }
}

