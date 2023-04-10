package mx.kenzie.centurion;

import mx.kenzie.centurion.arguments.*;

import javax.annotation.Nonnull;
import java.util.*;

public class Arguments implements Iterable<Object> {
  public static final TypedArgument<ArgumentContainer> PATTERN = new PatternArgument();
  public static final TypedArgument<Class> CLASS = new ArgClass();
  public static final TypedArgument<Integer> INTEGER = new ArgInteger();
  public static final TypedArgument<Long> LONG = new ArgLong();
  public static final TypedArgument<Double> DOUBLE = new ArgDouble();
  public static final TypedArgument<Boolean> BOOLEAN = new ArgBoolean();
  public static final TypedArgument<String> STRING = new ArgString();
  public static final TypedArgument<String> GREEDY_STRING = new ArgString() {

    @Override
    public int weight() {
      return super.weight() + 20;
    }

    @Override
    public boolean plural() {
      return true;
    }

  };
  public static final TypedArgument<Argument> ARGUMENT = new ArgumentArgument();

  private final List<Object> values;
  private final Map<Argument<?>, Object> map;

  Arguments(Object... values) {
    this.values = Arrays.asList(values);
    this.map = new HashMap<>();
  }

  public Arguments(ArgumentContainer container, Object... values) {
    this.values = Arrays.asList(values);
    this.map = new HashMap<>();
    final Command<?>.Context context = Command.getContext();
    if (context == null || context.getCommand() == null) this.unwrapArguments(container, false);
    else this.unwrapArguments(container, context.getCommand().behaviour().passAllArguments());
  }

  private void unwrapArguments(ArgumentContainer container, boolean passAllArguments) {
    final Iterator<Object> iterator = values.iterator();
    for (Argument<?> argument : container.arguments) {
      if (!passAllArguments && argument.literal()) continue;
      if (!iterator.hasNext()) break;
      this.map.put(argument, iterator.next());
    }
  }

  @SuppressWarnings("unchecked")
  public <Type> Type get(int index) {
    if (index >= values.size()) return null;
    return (Type) values.get(index);
  }

  @SuppressWarnings("unchecked")
  public <Type> Type get(Class<Type> type) {
    for (Object value : values) if (type.isInstance(value)) return (Type) value;
    return null;
  }

  @SuppressWarnings("unchecked")
  public <Type> Type get(Class<Type> type, int index) {
    for (Object value : values) {
      if (!type.isInstance(value)) continue;
      if (--index < 0) return (Type) value;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <Type> Type get(Argument<Type> type) {
    return (Type) map.get(type);
  }

  @Override
  public @Nonnull Iterator<Object> iterator() {
    return values.iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Arguments)) return false;
    return Objects.equals(values, ((Arguments) o).values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(values);
  }

  public int size() {
    return values.size();
  }

  public boolean isEmpty() {
    return values.isEmpty();
  }

  @Override
  public String toString() {
    return "Arguments" + values;
  }

}

