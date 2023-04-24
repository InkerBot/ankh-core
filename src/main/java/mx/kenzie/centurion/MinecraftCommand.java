package mx.kenzie.centurion;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mx.kenzie.centurion.arguments.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.inksnow.ankh.core.common.AdventureAudiences;
import org.inksnow.ankh.core.common.util.BootstrapUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public abstract class MinecraftCommand extends Command<CommandSender> implements TabCompleter, CommandExecutor {
  public static final EnumArgument<BlockFace> BLOCK_FACE = new EnumArgument<>(BlockFace.class);
  public static final EnumArgument<Material> MATERIAL = new EnumArgument<>(Material.class);
  public static final EnumArgument<EntityType> ENTITY_TYPE = new EnumArgument<>(EntityType.class);
  public static final TypedArgument<TextColor> COLOR = new ColorArgument().labelled("color");
  public static final TypedArgument<Player> PLAYER = new PlayerArgument();
  public static final TypedArgument<World> WORLD = new WorldArgument();
  public static final TypedArgument<Key> KEY = new KeyArgument();
  public static final TypedArgument<NamespacedKey> NAMESPACED_KEY = new NamespacedKeyArgument();
  public static final TypedArgument<RelativeNumber> RELATIVE_NUMBER = new RelativeNumberArgument(),
      LOCAL_NUMBER = new LocalNumberArgument();
  public static final TypedArgument<Vector> VECTOR = new CompoundArgument<>("vector", Vector.class)
      .arg(Arguments.DOUBLE.labelled("x"), Arguments.DOUBLE.labelled("y"), Arguments.DOUBLE.labelled("z"), arguments -> new Vector(arguments.<Double>get(0), arguments.get(1), arguments.get(2)));
  public static final TypedArgument<Location> LOCATION = new CompoundArgument<>("location", Location.class)
      .arg(VECTOR, "in", WORLD, arguments -> new Location(arguments.get(3), arguments.<Double>get(0), arguments.get(1), arguments.get(2)))
      .arg("spawn", "of", WORLD, arguments -> arguments.<World>get(0).getSpawnLocation())
      .arg("bed", "of", PLAYER, arguments -> arguments.<Player>get(0).getBedSpawnLocation());
  public static final TypedArgument<RelativeVector> OFFSET = new CompoundArgument<>("offset", RelativeVector.class)
      .arg(RELATIVE_NUMBER.labelled("x"), RELATIVE_NUMBER.labelled("y"), RELATIVE_NUMBER.labelled("z"), arguments -> new RelativeVector(arguments.get(0), arguments.get(1), arguments.get(2)));
  public static final TypedArgument<LocalVector> LOCAL_OFFSET = new CompoundArgument<>("local", LocalVector.class)
      .arg(LOCAL_NUMBER.labelled("left"), LOCAL_NUMBER.labelled("up"), LOCAL_NUMBER.labelled("forwards"), arguments -> new LocalVector(arguments.get(0), arguments.get(1), arguments.get(2)));
  public static final ColorProfile DEFAULT_PROFILE = new ColorProfile(
      NamedTextColor.WHITE,
      NamedTextColor.DARK_GREEN,
      NamedTextColor.GREEN,
      NamedTextColor.GOLD
  );
  private static final MethodHandle pluginCommandInitHandle = BootstrapUtil.ofInit(
      "Lorg/bukkit/command/PluginCommand;<init>(Ljava/lang/String;Lorg/bukkit/plugin/Plugin;)V"
  );
  private static final MethodHandle serverGetCommandMapHandle = BootstrapUtil.ofGet(
      "Lorg/bukkit/craftbukkit/[CB_VERSION]/CraftServer;commandMap:Lorg/bukkit/craftbukkit/[CB_VERSION]/command/CraftCommandMap;"
  ).asType(MethodType.methodType(CommandMap.class, Server.class));
  @Getter
  @Setter
  private String usage;
  @Getter
  @Setter
  private String permission;
  @Getter
  private Component permissionMessage;
  private String legacyPermissionMessage;

  {
    if (behaviour().lapse() == Command.BEHAVIOR_DEFAULT_LAPSE) {
      behaviour().lapse(this::printUsage);
    }
  }

  protected MinecraftCommand(String description) {
    super();
    this.description = description;
    this.usage = '/' + behaviour().label();
    this.permission = null;
    permissionMessage(this.createPermissionMessage());
  }

  protected MinecraftCommand(String description, String usage, String permission, Component permissionMessage) {
    super();
    this.description = description;
    this.usage = usage;
    this.permission = permission;
    permissionMessage(permissionMessage);
  }

  protected Component createPermissionMessage() {
    final ColorProfile profile = this.getProfile();
    return Component.text()
        .append(Component.text("!! ", profile.pop()))
        .append(Component.translatable("commands.help.failed", profile.highlight()))
        .build();
  }

  protected CommandResult printUsage(CommandSender sender, Arguments arguments) {
    final ColorProfile profile = this.getProfile();
    final TextComponent.Builder builder = Component.text();
    builder.append(Component.text("Usage for ", profile.dark()))
        .append(Component.text("/" + behaviour().label(), profile.highlight()))
        .append(Component.text(":", profile.dark()));
    for (ArgumentContainer container : behaviour().arguments()) {
      final Component hover;
      final ClickEvent click;
      if (container.hasInput()) {
        hover = Component.text("Click to Suggest");
        final StringBuilder text = new StringBuilder("/" + behaviour().label());
        for (Argument<?> argument : container.arguments()) {
          if (!argument.literal()) break;
          text.append(' ').append(argument.label());
        }
        click = ClickEvent.suggestCommand(text.toString());
      } else {
        hover = Component.text("Click to Run");
        click = ClickEvent.runCommand("/" + behaviour().label() + container);
      }
      final Component line = Component.text()
          .append(Component.text("/", profile.pop()))
          .append(Component.text(behaviour().label(), profile.light()))
          .append(this.print(container))
          .hoverEvent(hover)
          .clickEvent(click)
          .build();
      builder.append(Component.newline())
          .append(Component.text("  "))
          .append(line);
    }
    AdventureAudiences.sender(sender).sendMessage(builder);
    return CommandResult.LAPSED;
  }

  protected Component print(ArgumentContainer container) {
    final TextComponent.Builder builder = Component.text();
    for (Argument<?> argument : container.arguments()) {
      builder.append(Component.space());
      builder.append(this.print(argument));
    }
    return builder.build();
  }

  protected Component print(Argument<?> argument) {
    final ColorProfile profile = this.getProfile();
    final TextComponent.Builder builder = Component.text();
    final boolean optional = argument.optional(), literal = argument.literal(), plural = argument.plural();
    final String label = argument.label();
    final String[] possibilities = argument.possibilities();
    if (optional) builder.append(Component.text('[', profile.pop()));
    else if (!literal) builder.append(Component.text('<', profile.pop()));
    if (argument instanceof CompoundArgument<?>) builder.append(Component.text('*', profile.pop()));
    if (possibilities.length > 0)
      builder.append(Component.text(label, profile.highlight()).insertion(possibilities[0]));
    else builder.append(Component.text(label, profile.highlight()));
    if (plural) builder.append(Component.text("...", profile.dark()));
    if (optional) builder.append(Component.text(']', profile.pop()));
    else if (!literal) builder.append(Component.text('>', profile.pop()));
    final Component component = builder.build();
    if (argument instanceof CompoundArgument<?>) return this.print((CompoundArgument<?>) argument, component);
    if (argument.description() == null) return component;
    return component.hoverEvent(Component.text(argument.description()));
  }

  private Component print(CompoundArgument<?> argument, Component component) {
    final TextComponent.Builder builder = Component.text();
    if (argument.description() != null) builder.append(Component.text(argument.description()));
    for (CompoundArgument.InnerContainer container : argument.arguments()) {
      builder.append(Component.newline());
      builder.append(this.print(container));
    }
    return component.hoverEvent(builder.build());
  }

  public void permissionMessage(Component permissionMessage) {
    this.permissionMessage = permissionMessage;
    this.legacyPermissionMessage = (permissionMessage == null)
        ? null
        : AdventureAudiences.serialize(permissionMessage);
  }

  @Override
  public boolean onCommand(@Nonnull CommandSender sender, @Nonnull org.bukkit.command.Command command, @Nonnull String label, @Nonnull String... args) {
    final String input;
    if (args == null || args.length < 1) input = label;
    else input = label + " " + String.join(" ", args);
    final Result result = this.execute(sender, input);
    if (result.error() != null) {
      logger.error("Error in command: {} {}", label, args, result.error());
    }
    return result.successful();
  }

  @Override
  public @Nullable List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull org.bukkit.command.Command command, @Nonnull String label, @Nonnull String... args) {
    final List<String> options = new ArrayList<>();
    Command.setContext(new Context(sender, String.join(" ", args)));
    if (args.length < 1) {
      for (ArgumentContainer argument : this.behaviour().arguments()) {
        options.addAll(Lists.newArrayList(argument.arguments()[0].possibilities()));
      }
    } else {
      final List<ArgumentContainer> containers = new LinkedList<>(this.behaviour().arguments());
      final String[] complete = new String[args.length - 1];
      final String current = args[args.length - 1].toLowerCase();
      System.arraycopy(args, 0, complete, 0, complete.length);
      final Iterator<ArgumentContainer> each = containers.iterator();
      arguments:
      while (each.hasNext()) {
        final ArgumentContainer next = each.next();
        final Argument<?>[] arguments = next.arguments();
        if (arguments.length <= complete.length) continue;
        int i = 0;
        for (; i < complete.length; i++) {
          final String test = complete[i];
          if (!arguments[i].matches(test)) continue arguments;
        }
        options.addAll(Lists.newArrayList(arguments[i].possibilities()));
      }
      final Iterator<String> iterator = options.iterator();
      while (iterator.hasNext()) {
        final String next = iterator.next();
        if (next.toLowerCase().startsWith(current)) continue;
        iterator.remove();
      }
    }
    Command.setContext(null);
    return options;
  }

  @SuppressWarnings("deprecation")
  @SneakyThrows
  public void register(Plugin plugin) {
    val command = (PluginCommand) pluginCommandInitHandle.invokeExact(behaviour().label(), plugin);
    this.update(command);
    command.register(this.getCommandMap());
    if (this.getCommandMap().register(behaviour().label(), plugin.getName(), command)) {
      command.setExecutor(this);
      command.setTabCompleter(this);
    } else {
      final org.bukkit.command.Command current = this.getCommandMap().getCommand(command.getName());
      if (current instanceof PluginCommand) {
        final PluginCommand found = (PluginCommand) current;
        this.update(found);
        found.setExecutor(this);
        found.setTabCompleter(this);
      }
      logger.warn("A command '/{}' is already defined!", behaviour().label());
      logger.warn("As this cannot be replaced, the executor will be overridden.");
      logger.warn("To avoid this warning, please do not add MinecraftCommands to your plugin.yml.");
    }
  }

  private void update(org.bukkit.command.Command command) {
    command.setAliases(new ArrayList<>(behaviour().aliases()));
    command.setDescription(this.description);
    command.setPermission(this.permission);
    // TODO: add modern permission message support
    command.setPermissionMessage(this.legacyPermissionMessage);
    command.setUsage(this.usage);
  }

  /**
   * This can be overridden if Bukkit removes or changes the method.
   */
  @SneakyThrows
  protected CommandMap getCommandMap() {
    return (CommandMap) serverGetCommandMapHandle.invokeExact(Bukkit.getServer());
  }

  /**
   * This can be overridden to change colours in default messages.
   */
  protected ColorProfile getProfile() {
    return DEFAULT_PROFILE;
  }
}
