package io.dkakunsi.bitapp.langchain;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import io.dkakunsi.bitapp.Configuration;
import io.dkakunsi.bitapp.LanguageModel;

public class GeminiLangChainModel implements LanguageModel {

  private static final String GEMINI_API_KEY = "ai.gemini.apiKey";

  private static final String GEMINI_MODEL_NAME = "api.gemini.modelName";

  private final ChatModel chatModel;

  private GeminiLangChainModel(ChatModel chatModel) {
    this.chatModel = chatModel;
  }

  @Override
  public String prompt(String promptMessage) {
    return chatModel.chat(promptMessage);
  }

  public static final LanguageModel create(Configuration configuration) {
    var apiKey = configuration.get(GEMINI_API_KEY).orElseThrow();
    var modelName = configuration.get(GEMINI_MODEL_NAME).orElseThrow();
    var chatModel = GoogleAiGeminiChatModel.builder()
        .apiKey(apiKey)
        .modelName(modelName)
        .build();

    return new GeminiLangChainModel(chatModel);
  }
}
