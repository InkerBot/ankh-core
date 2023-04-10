package mx.kenzie.centurion.arguments;

import java.util.regex.Pattern;

public class ArgClass extends HashedArg<Class> {
  private static final Pattern PART = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");
  private static final Pattern CLASS = Pattern.compile(PART + "(\\." + PART + ")*");

  public ArgClass() {
    super(Class.class);
  }

  @Override
  public boolean matches(String input) {
    if (lastHash == input.hashCode() && lastValue != null) return true;
    this.lastHash = input.hashCode();
    this.lastValue = null;
    if (!CLASS.matcher(input).matches()) return false;
    return (lastValue = this.parseNew(input)) != null;
  }

  @Override
  public Class<?> parseNew(String input) {
    try {
      return Class.forName(input.trim());
    } catch (ClassNotFoundException ex) {
      return null;
    }
  }


}
