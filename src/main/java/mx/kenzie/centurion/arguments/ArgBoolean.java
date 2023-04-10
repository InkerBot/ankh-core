package mx.kenzie.centurion.arguments;

public class ArgBoolean extends TypedArgument<Boolean> {
  public ArgBoolean() {
    super(Boolean.class);
  }

  @Override
  public boolean matches(String input) {
    final String parsed = input.toLowerCase().trim();
    return parsed.equals("true") || parsed.equals("false");
  }

  @Override
  public Boolean parse(String input) {
    return input.trim().equalsIgnoreCase("true");
  }

  @Override
  public String[] possibilities() {
    return new String[]{"true", "false"};
  }

  @Override
  public Boolean lapse() {
    return false;
  }

}
