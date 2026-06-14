package com.autodev.auth.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UID;

/**
 * Сущность для хранения ролей в базе данных.
 * Enum Role используется как тип роли, а не как сущность JPA.
 */
@Entity
@Table(name = "roles")
public class Role {


}
