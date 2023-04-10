package mx.kenzie.centurion.arguments;

public class ArgLong extends HashedArg<Long> {

  public ArgLong() {
    super(Long.class);
  }

  @Override
  public boolean matches(String input) {
    this.lastHash = input.hashCode();
    this.lastValue = null;
    for (char c : input.toCharArray()) if ((c < '0' || c > '9') && c != '-') return false;
    try {
      this.lastValue = Long.parseLong(input);
      return true;
    } catch (Throwable ex) {
      return false;
    }
  }

  @Override
  public Long parseNew(String input) {
    return Long.parseLong(input.trim());
  }
}
