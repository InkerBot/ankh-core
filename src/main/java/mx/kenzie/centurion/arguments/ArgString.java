package mx.kenzie.centurion.arguments;

public class ArgString extends TypedArgument<String> {

  public ArgString() {
    super(String.class);
  }

  @Override
  public boolean matches(String input) {
    return input.length() > 0;
  }

  @Override
  public String parse(String input) {
    return input;
  }

  @Override
  public int weight() {
    return super.weight() + 10;
  }

}
