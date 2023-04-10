package mx.kenzie.centurion.arguments;

public abstract class HashedArg<Type> extends TypedArgument<Type> {
  protected int lastHash;
  protected Type lastValue;

  public HashedArg(Class<Type> type) {
    super(type);
  }

  @Override
  public Type parse(String input) {
    if (lastHash == input.hashCode() && lastValue != null) return lastValue;
    return this.parseNew(input);
  }

  public abstract Type parseNew(String input);

}
