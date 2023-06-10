package org.inksnow.ankh.cloud;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public final class AnkhCloudEula {
  private static final Path EULA_PATH = Paths.get("plugins", "ankh-core", "ankh-data-collect.txt");
  @Getter
  @Setter
  private static boolean enabled = false;
  @Getter
  @Setter
  private static UUID serverId = null;

  private AnkhCloudEula() {
    throw new UnsupportedOperationException();
  }

  public static void init() throws IOException {
    if (Files.notExists(EULA_PATH)) {
      save();
      load();
    } else {
      load();
      save();
    }
  }

  public static void save() throws IOException {
    Files.createDirectories(EULA_PATH.getParent());
    try (Writer writer = Files.newBufferedWriter(EULA_PATH)) {
      writer.append("# Ankh Core will collect information, including online player\n")
          .append("# data, server performance metrics, server loads, and exception\n")
          .append("# logs, from your servers for data analysis. The collected data\n")
          .append("# will be utilized to improve and optimize Ankh Core, ultimately\n")
          .append("# enhancing user experience and service quality. We assure that\n")
          .append("# this data will not be shared with any third parties. Users can\n")
          .append("# disable this data collection by set the \"enable\" section to\n")
          .append("# \"false\" below, please note that in non-release versions, we\n")
          .append("# won't authorize user who disable this feature. e apologize for\n")
          .append("# any inconvenience this may cause.\n")
          .append("\n")
          .append("enabled=").append(Boolean.toString(enabled)).append("\n")
          .append("serverId=").append(serverId == null ? "" : serverId.toString()).append("\n");
    }
    load();
  }

  public static void load() throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(EULA_PATH)) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("#")) {
          continue;
        }
        int splitIndex = line.indexOf('=');
        if (splitIndex == -1) {
          continue;
        }
        String key = line.substring(0, splitIndex);
        String value = line.substring(splitIndex + 1);

        switch (key) {
          case "enabled": {
            enabled = Boolean.parseBoolean(value);
            break;
          }
          case "serverId": {
            serverId = value.isEmpty() ? null : UUID.fromString(value);
            break;
          }
        }
      }
    }
  }
}
