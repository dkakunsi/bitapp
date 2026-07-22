package io.dkakunsi.bitapp;

public interface LanguageModel {

  String prompt(String promptMessage);

  public static class LanguageModelFactory {

    protected static LanguageModel testLangChainModel;

    public static void setTestLangChainModel(LanguageModel model) {
      testLangChainModel = model;
    }
  }
}
