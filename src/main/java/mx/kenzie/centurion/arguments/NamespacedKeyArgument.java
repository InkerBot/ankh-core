package mx.kenzie.centurion.arguments;

import lombok.val;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;

import java.util.regex.Pattern;

public class NamespacedKeyArgument extends TypedArgument<NamespacedKey> {
  private static final Pattern VALID_NAMESPACE = Pattern.compile("[a-z0-9._-]+");
  private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9/._-]+");

  public NamespacedKeyArgument() {
    super(NamespacedKey.class);
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
  public NamespacedKey parse(String input) {
    val key = Key.key(input.trim());
    return new NamespacedKey(key.namespace(), key.value());
  }
}
