package io.dkakunsi.bitapp.mongo.entity;

import java.time.LocalDateTime;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.common.ModelStatus;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.model.User.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity("users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

  @dev.morphia.annotations.Id
  private String id;
  private String name;
  private String phone;
  private String photoUrl;
  private String language;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  /**
   * Converts this entity to a domain User model.
   */
  public User toUser() {
    return User.builder()
        .id(Id.of(this.id))
        .name(this.name)
        .phone(this.phone)
        .photoUrl(this.photoUrl)
        .language(Language.valueOf(this.language))
        .status(ModelStatus.valueOf(this.status))
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .createdBy(this.createdBy)
        .updatedBy(this.updatedBy)
        .build();
  }

  /**
   * Creates an entity from a domain User model.
   */
  public static UserEntity fromUser(User user) {
    // Morphia is not working properly with builder pattern, so we have to use the
    // constructor
    return new UserEntity(
        user.id().value(),
        user.name(),
        user.phone(),
        user.photoUrl(),
        user.language().name(),
        user.status().name(),
        user.createdAt(),
        user.updatedAt(),
        user.createdBy(),
        user.updatedBy());
  }
}