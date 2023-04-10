package mx.kenzie.centurion;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TagArgument<Type extends Keyed> extends HashedArg<Tag<Type>> {
  private static final Map<String, Tag<Material>> MATERIALS = new HashMap<>();
  private static final Map<String, Tag<Material>> ITEMS = new HashMap<>();
  private static final Map<String, Tag<EntityType>> ENTITIES = new HashMap<>();

  static {
    for (Field field : Tag.class.getDeclaredFields()) {
      try {
        if (field.getType() == Tag.class) {
          Object tag = field.get(null);
          if (tag != null) {
            MATERIALS.put(field.getName(), (Tag<Material>) tag);
          }
        }
      } catch (Exception e) {
        logger.error("Failed to get tag {} in {}", field.getName(), Tag.class, e);
      }
    }
  }

  protected final Map<String, Tag<Type>> map;

  @SuppressWarnings("unchecked")
  public TagArgument(Class<Type> type, Map<String, Tag<Type>> map) {
    super((Class<Tag<Type>>) (Class) Tag.class);
    this.map = map;
    this.label = type.getSimpleName().toLowerCase() + "s";
  }

  public static TagArgument<Material> materials() {
    return new TagArgument<>(Material.class, MATERIALS);
  }

  public static TagArgument<Material> items() {
    return new TagArgument<>(Material.class, ITEMS);
  }

  public static TagArgument<EntityType> entities() {
    return new TagArgument<>(EntityType.class, ENTITIES);
  }

  @Override
  public Tag<Type> parseNew(String input) {
    return lastValue = map.get(input.substring(1).toUpperCase());
  }

  @Override
  public String[] possibilities() {
    if (possibilities != null && possibilities.length > 0) return possibilities;
    final List<String> list = new ArrayList<>(map.size());
    for (String key : map.keySet()) list.add("#" + key.toLowerCase());
    return possibilities = list.toArray(new String[0]);
  }

  @Override
  public boolean matches(String input) {
    if (input.charAt(0) != '#') return false;
    this.lastHash = input.hashCode();
    this.lastValue = null;
    return this.parseNew(input) != null;
  }

}
