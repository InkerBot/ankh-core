package org.inksnow.ankh.core.config.yml;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigSectionFactory;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Slf4j
public class SnakeYmlConfigFactory implements ConfigSectionFactory {
  public static final String INTERNAL_EXTENSION_PREFIX = "_$ankh$extension$internal$_";
  private static final Object UNIT_OBJECT = new Object();
  private static final ThreadLocal<Yaml> yaml = ThreadLocal.withInitial(SnakeYmlConfigFactory::createSafeYaml);

  private static Yaml createSafeYaml() {
    val loadingConfig = new LoaderOptions();
    loadingConfig.setProcessComments(true);
    loadingConfig.setAllowDuplicateKeys(true);
    val dumperOptions = new DumperOptions();
    val constructor = new AnkhConstructor(loadingConfig);
    val representer = new Representer(dumperOptions);
    val resolver = new Resolver();
    return new Yaml(constructor, representer, dumperOptions, loadingConfig, resolver);
  }

  private static <T> Stream<T> contactStream(Collection<T>... streams) {
    Stream<T> currentStream = null;
    for (val stream : streams) {
      if (stream == null) {
        continue;
      }
      if (currentStream == null) {
        currentStream = stream.stream();
      } else {
        currentStream = Stream.concat(currentStream, stream.stream());
      }
    }
    return currentStream;
  }

  @Override
  public @Nonnull ConfigSection load(@Nonnull ConfigSource source, @Nonnull Reader reader) {
    return new YamlConfigSection(source, yaml.get().load(reader));
  }

  private static class AnkhConstructor extends SafeConstructor {
    public AnkhConstructor(LoaderOptions loadingConfig) {
      super(loadingConfig);
      yamlConstructors.put(new Tag("!include"), new ImportConstruct());
      yamlConstructors.put(Tag.MAP, new MapImportConstruct(yamlConstructors.get(Tag.MAP)));
    }
  }

  private static class ImportConstruct extends AbstractConstruct {
    @Override
    public Object construct(Node node) {
      if (!(node instanceof ScalarNode)) {
        throw new IllegalArgumentException("Non-scalar !import: " + node.toString());
      }

      final ScalarNode scalarNode = (ScalarNode) node;
      final String value = scalarNode.getValue();

      return ImmutableMap.builder()
          .put(INTERNAL_EXTENSION_PREFIX, ImmutableMap.builder()
              .put(value, UNIT_OBJECT)
              .build()
          )
          .build();
    }
  }

  @RequiredArgsConstructor
  private static class MapImportConstruct implements Construct {
    private final Construct delegate;

    @Override
    public Object construct(Node node) {
      val result = (Map<String, Object>) delegate.construct(node);
      if (node instanceof MappingNode) {
        val mnode = (MappingNode) node;
        val commentInclude = mnode.getValue()
            .stream()
            .flatMap(it -> contactStream(
                it.getKeyNode().getBlockComments(),
                it.getKeyNode().getEndComments(),
                it.getValueNode().getBlockComments(),
                it.getValueNode().getEndComments()
            ))
            .map(CommentLine::getValue)
            .filter(it -> it.startsWith("!include "))
            .collect(Collectors.toMap(it -> it.substring("!include ".length()), it -> UNIT_OBJECT));
        if (!commentInclude.isEmpty()) {
          val extensionMapBuilder = ImmutableMap.<String, Object>builder();
          val rawMap = (Map<String, Object>) result.get(INTERNAL_EXTENSION_PREFIX);
          if (rawMap != null) {
            extensionMapBuilder.putAll(rawMap);
          }
          extensionMapBuilder.putAll(commentInclude);
          result.put(INTERNAL_EXTENSION_PREFIX, extensionMapBuilder.build());
        }
      }
      return result;
    }

    @Override
    public void construct2ndStep(Node node, Object object) {
      delegate.construct2ndStep(node, object);
    }
  }
}
