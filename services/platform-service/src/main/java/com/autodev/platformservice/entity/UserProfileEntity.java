package com.autodev.platformservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_profiles", schema = "platform")
public class UserProfileEntity extends AbstractBaseEntity {

    /**Идентификатор пользователя в keycloak */
    @Column(name = "keycloak_user_id",
            nullable = false, unique = true)
    private String keycloakUserId;

    /** Адрес электронной почты пользователя */
    @Column(name = "email",
            unique = true, nullable = false)
    private String email;

    /** Название магазина или разборки */
    @Column(name = "store_name")
    private String storeName;

    /** Описание магазина (часы работы, условия доставки и т.д.)*/
    @Column(name = "store_description",
            columnDefinition = "TEXT")
    private String storeDescription;

    /** Ссылка на логотип магазина. Хранит не сам файл, а URL пути в объектном хранилище MinIO */
    @Column(name = "store_logo_url")
    private String storeLogoUrl;

    /** Ссылка на аватар пользователя */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /** Статус верификации продавца */
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status",
            length = 50)
    private VerificationStatus verificationStatus;

    /** Баланс баллов лояльности продавца (или покупателя). */
    @Column(name = "loyalty_balance")
    private BigDecimal loyaltyBalance;




}