package io.dkakunsi.bitapp.mongo;

import java.util.Optional;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

import io.dkakunsi.bitapp.common.Id;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.model.User.Language;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public class MongoUserRepository implements UserRepository {

  private static final String COLLECTION_NAME = "users";
  private static final String FIELD_ID = "id";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_PHONE = "phone";
  private static final String FIELD_PHOTO_URL = "photoUrl";
  private static final String FIELD_LANGUAGE = "language";

  private final MongoCollection<Document> collection;

  public MongoUserRepository(MongoDatabase database) {
    this.collection = database.getCollection(COLLECTION_NAME);
  }

  @Override
  public User save(User user) {
    var document = toDocument(user);
    var options = new ReplaceOptions().upsert(true);
    collection.replaceOne(
        Filters.eq(FIELD_ID, user.getId().value()),
        document,
        options);
    return user;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    var document = collection.find(Filters.eq(FIELD_ID, email)).first();
    return Optional.ofNullable(document).map(this::toUser);
  }

  private Document toDocument(User user) {
    var document = new Document();
    document.append(FIELD_ID, user.getId().value());
    document.append(FIELD_NAME, user.getName());
    document.append(FIELD_PHONE, user.getPhone());
    document.append(FIELD_PHOTO_URL, user.getPhotoUrl());
    document.append(FIELD_LANGUAGE, user.getLanguage().name());
    return document;
  }

  private User toUser(Document document) {
    return User.builder()
        .id(Id.of(document.getString(FIELD_ID)))
        .name(document.getString(FIELD_NAME))
        .phone(document.getString(FIELD_PHONE))
        .photoUrl(document.getString(FIELD_PHOTO_URL))
        .language(Language.valueOf(document.getString(FIELD_LANGUAGE)))
        .build();
  }
}
