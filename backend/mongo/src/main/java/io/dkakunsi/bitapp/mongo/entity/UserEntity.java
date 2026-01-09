package io.dkakunsi.bitapp.mongo.entity;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.common.Id;
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
        .build();
  }

  /**
   * Creates an entity from a domain User model.
   */
  public static UserEntity fromUser(User user) {
    return UserEntity.builder()
        .id(user.getId().value())
        .name(user.getName())
        .phone(user.getPhone())
        .photoUrl(user.getPhotoUrl())
        .language(user.getLanguage().name())
        .build();
  }
}