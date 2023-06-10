package org.inksnow.ankh.cloud.endpoint;

import lombok.*;
import org.inksnow.ankh.cloud.AnkhCloudEnvironment;
import org.inksnow.ankh.cloud.AnkhCloudEula;
import org.inksnow.ankh.cloud.bean.LogBean;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public final class PostLogRequest {
  public static final URI URL = URI.create(AnkhCloudEnvironment.BASE_URL + "/endpoint/postlog");

  private final UUID serverId = AnkhCloudEula.serverId();

  private final List<LogBean> entries;
}
