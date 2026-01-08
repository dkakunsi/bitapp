package io.dkakunsi.bitapp.mongo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.dkakunsi.bitapp.account.model.Account;
import io.dkakunsi.bitapp.account.repository.AccountRepository;

public class MongoAccountRepository implements AccountRepository {

  private static final String COLLECTION_NAME = "accounts";
  private static final String FIELD_ID = "id";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_TYPE = "type";
  private static final String FIELD_THEME_COLOR = "themeColor";
  private static final String FIELD_BALANCE = "balance";
  private static final String FIELD_USER_ID = "userId";
  private static final String FIELD_CREATED_AT = "createdAt";
  private static final String FIELD_UPDATED_AT = "updatedAt";
  private static final String FIELD_CREATED_BY = "createdBy";
  private static final String FIELD_UPDATED_BY = "updatedBy";

  private final MongoCollection<Document> collection;

  public MongoAccountRepository(MongoDatabase database) {
    this.collection = database.getCollection(COLLECTION_NAME);
  }

  @Override
  public Account create(Account account) {
    var document = toDocument(account);
    collection.insertOne(document);
    return account;
  }

  private Document toDocument(Account account) {
    var document = new Document();
    document.append(FIELD_ID, account.getId().value());
    document.append(FIELD_NAME, account.getName());
    document.append(FIELD_TYPE, account.getType().name());
    document.append(FIELD_THEME_COLOR, account.getThemeColor());
    document.append(FIELD_BALANCE, account.getBalance().doubleValue());
    document.append(FIELD_USER_ID, account.getUser().getId().value());
    document.append(FIELD_CREATED_AT, toDate(account.getCreatedAt()));
    document.append(FIELD_UPDATED_AT, toDate(account.getUpdatedAt()));
    document.append(FIELD_CREATED_BY, account.getCreatedBy());
    document.append(FIELD_UPDATED_BY, account.getUpdatedBy());
    return document;
  }

  private Date toDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }
}
