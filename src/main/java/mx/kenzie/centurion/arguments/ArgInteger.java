package mx.kenzie.centurion.arguments;

public class ArgInteger extends HashedArg<Integer> {

  public ArgInteger() {
    super(Integer.class);
    this.label = "int";
  }

  @Override
  public boolean matches(String input) {
    this.lastHash = input.hashCode();
    for (char c : input.toCharArray()) if ((c < '0' || c > '9') && c != '-') return false;
    try {
      this.lastValue = Integer.parseInt(input);
      return true;
    } catch (Throwable ex) {
      return false;
    }
  }

  @Override
  public Integer parseNew(String input) {
    return Integer.parseInt(input.trim());
  }
}
