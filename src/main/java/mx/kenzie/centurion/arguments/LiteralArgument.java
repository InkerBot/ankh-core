package mx.kenzie.centurion.arguments;

import lombok.Data;
import mx.kenzie.centurion.Argument;

@Data
public class LiteralArgument implements Argument<String> {
  private final String label;

  @Override
  public boolean matches(String input) {
    return input.equals(label);
  }

  @Override
  public String parse(String input) {
    return label;
  }

  @Override
  public boolean literal() {
    return true;
  }

  @Override
  public int weight() {
    return 3;
  }

  @Override
  public String[] possibilities() {
    return new String[]{label};
  }

}
