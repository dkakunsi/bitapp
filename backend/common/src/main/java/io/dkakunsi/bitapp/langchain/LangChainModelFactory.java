package io.dkakunsi.bitapp.langchain;

import io.dkakunsi.bitapp.Configuration;
import io.dkakunsi.bitapp.LanguageModel;

public class LangChainModelFactory extends LanguageModel.LanguageModelFactory {
  private static final String MODEL_TYPE = "ai.model";

  public static LanguageModel createLangChainModel(Configuration configuration) {
    var modelType = configuration.get(MODEL_TYPE)
        .orElse("");

    return switch (modelType) {
      case "gemini" -> GeminiLangChainModel.create(configuration);
      case "test" -> testLangChainModel;
      default -> throw new IllegalArgumentException("Unsupported model type: " + modelType);
    };
  }
}
