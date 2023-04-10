package mx.kenzie.centurion.arguments;

public class ArgDouble extends HashedArg<Double> {

  public ArgDouble() {
    super(Double.class);
    this.label = "number";
  }

  @Override
  public boolean matches(String input) {
    this.lastHash = input.hashCode();
    this.lastValue = null;
    try {
      this.lastValue = this.parseNew(input);
      return true;
    } catch (Throwable ex) {
      return false;
    }
  }

  @Override
  public Double parseNew(String input) {
    if (input.endsWith("D") || input.endsWith("d"))
      return Double.parseDouble(input.substring(0, input.length() - 1));
    return Double.parseDouble(input.trim());
  }
}
