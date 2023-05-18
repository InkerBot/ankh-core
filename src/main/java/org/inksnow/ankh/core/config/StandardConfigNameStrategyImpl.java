package org.inksnow.ankh.core.config;

import lombok.RequiredArgsConstructor;
import org.inksnow.ankh.core.api.config.ConfigNameStrategy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.Locale;

@RequiredArgsConstructor
public class StandardConfigNameStrategyImpl implements ConfigNameStrategy {
  private final @Nonnull CaseType caseType;
  private final @Nullable String separator;

  private static String upperCaseFirstLetter(String name) {
    StringBuilder fieldNameBuilder = new StringBuilder();
    int index = 0;
    char firstCharacter = name.charAt(index);

    while (index < name.length() - 1) {
      if (Character.isLetter(firstCharacter)) {
        break;
      }

      fieldNameBuilder.append(firstCharacter);
      firstCharacter = name.charAt(++index);
    }

    if (index == name.length()) {
      return fieldNameBuilder.toString();
    }

    if (!Character.isUpperCase(firstCharacter)) {
      String modifiedTarget = modifyString(Character.toUpperCase(firstCharacter), name, ++index);
      return fieldNameBuilder.append(modifiedTarget).toString();
    } else {
      return name;
    }
  }

  private static String separateCamelCase(String name, String separator) {
    StringBuilder translation = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char character = name.charAt(i);
      if (Character.isUpperCase(character) && translation.length() != 0) {
        translation.append(separator);
      }
      translation.append(character);
    }
    return translation.toString();
  }

  private static String modifyString(char firstCharacter, String srcString, int indexOfSubstring) {
    return (indexOfSubstring < srcString.length())
        ? firstCharacter + srcString.substring(indexOfSubstring)
        : String.valueOf(firstCharacter);
  }

  @Nonnull
  @Override
  public String translateName(@Nonnull String name) {
    if (separator != null) {
      name = separateCamelCase(name, separator);
    }
    switch (caseType) {
      case UPPER_FIRST: {
        name = upperCaseFirstLetter(name);
        break;
      }
      case LOWER_ALL: {
        name = name.toLowerCase(Locale.ENGLISH);
        break;
      }
      case UPPER_ALL: {
        name = name.toUpperCase(Locale.ENGLISH);
      }
    }
    return name;
  }

  private enum CaseType {
    UPPER_FIRST,
    LOWER_ALL,
    UPPER_ALL,
    NONE
  }

  @Singleton
  public static class Factory implements ConfigNameStrategy.Factory {
    private static final ConfigNameStrategy IDENTITY = new Builder()
        .build();

    private static final ConfigNameStrategy UPPER_CAMEL_CASE = new Builder()
        .upperCaseFirstLetter()
        .build();

    private static final ConfigNameStrategy UPPER_CAMEL_CASE_WITH_SPACES = new Builder()
        .upperCaseFirstLetter()
        .separateCamelCase(" ")
        .build();

    private static final ConfigNameStrategy LOWER_CASE_WITH_UNDERSCORES = new Builder()
        .lowerCaseAllLetter()
        .separateCamelCase("_")
        .build();

    private static final ConfigNameStrategy LOWER_CASE_WITH_DASHES = new Builder()
        .lowerCaseAllLetter()
        .separateCamelCase("-")
        .build();

    @Override
    public @Nonnull Builder builder() {
      return new Builder();
    }

    @Override
    public @Nonnull ConfigNameStrategy identity() {
      return IDENTITY;
    }

    @Override
    public @Nonnull ConfigNameStrategy upperCamelCase() {
      return UPPER_CAMEL_CASE;
    }

    @Override
    public @Nonnull ConfigNameStrategy upperCamelCaseWithSpaces() {
      return UPPER_CAMEL_CASE_WITH_SPACES;
    }

    @Override
    public @Nonnull ConfigNameStrategy lowerCaseWithUnderscores() {
      return LOWER_CASE_WITH_UNDERSCORES;
    }

    @Override
    public @Nonnull ConfigNameStrategy lowerCaseWithDashes() {
      return LOWER_CASE_WITH_DASHES;
    }
  }

  public static class Builder implements ConfigNameStrategy.Builder {
    private CaseType caseType = CaseType.NONE;
    private String separator = null;

    @Override
    public @Nonnull Builder upperCaseFirstLetter() {
      this.caseType = CaseType.UPPER_FIRST;
      return this;
    }

    @Override
    public @Nonnull Builder lowerCaseAllLetter() {
      this.caseType = CaseType.LOWER_ALL;
      return this;
    }

    @Nonnull
    @Override
    public ConfigNameStrategy.Builder upperCaseAllLetter() {
      this.caseType = CaseType.UPPER_ALL;
      return this;
    }

    @Override
    public @Nonnull Builder separateCamelCase(@Nonnull String separator) {
      this.separator = separator;
      return this;
    }

    @Override
    public @Nonnull Builder getThis() {
      return this;
    }

    @Override
    public @Nonnull ConfigNameStrategy build() {
      return new StandardConfigNameStrategyImpl(caseType, separator);
    }
  }
}
