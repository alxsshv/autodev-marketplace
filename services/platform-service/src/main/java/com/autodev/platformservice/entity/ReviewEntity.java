package com.autodev.platformservice.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reviews", schema = "platform")
@SuppressWarnings("java:S2160")
public class ReviewEntity extends AbstractBaseEntity {

    @Column(name = "product_id",
            nullable = false)
    private UUID productId;

    @Column(name = "user_id",
            nullable = false)
    private UUID userId;

    @Column(name = "rating",
            nullable = false,
            columnDefinition = "int2")
    private Integer rating;

    @Column(name = "review_text",
            columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "seller_reply",
            columnDefinition = "TEXT")
    private String sellerReply;

}
