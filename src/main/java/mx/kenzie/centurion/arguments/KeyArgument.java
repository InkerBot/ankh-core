package mx.kenzie.centurion.arguments;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;

public class KeyArgument extends TypedArgument<Key> {
  public KeyArgument() {
    super(Key.class);
  }

  @Override
  public boolean matches(String input) {
    try {
      Key.key(input.trim());
      return true;
    } catch (InvalidKeyException e) {
      return false;
    }
  }

  @Override
  public Key parse(String input) {
    return Key.key(input.trim());
  }
}
