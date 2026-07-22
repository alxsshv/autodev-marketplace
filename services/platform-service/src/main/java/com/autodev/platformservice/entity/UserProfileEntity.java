package com.autodev.platformservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profiles", schema = "platform")
public class UserProfileEntity extends AbstractBaseEntity {

    /**Идентификатор пользователяв keycloak*/
    @Column(name = "keycloak_user_id",
            nullable = false, unique = true)
    private UUID keycloakUserId;

    /** Название магазина или разборки */
    @Column(name = "store_name",
            length = 255)
    private String storeName;

    /** Описание магазина (часы работы, условия доставки и т.д.)*/
    @Column(name = "store_description",
            columnDefinition = "TEXT")
    private String storeDescription;

    /** Ссылка на логотип магазина. Хранит не сам файл, а URL пути в объектном хранилище MinIO */
    @Column(name = "store_logo_url",
            length = 255)
    private String storeLogoUrl;

    /** Ссылка на аватар пользователя */
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    /** Статус верификации продавца */
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status",
            length = 50)
    private VerificationStatus verificationStatus;

    /** Баланс баллов лояльности продавца (или покупателя). */
    @Column(name = "loyalty_balance")
    private Long loyaltyBalance;


}