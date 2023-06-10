package org.inksnow.ankh.cloud.endpoint;

import lombok.*;
import org.inksnow.ankh.cloud.AnkhCloudEnvironment;
import org.inksnow.ankh.cloud.AnkhCloudEula;

import java.net.URI;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public final class LoginRequest {
  public static final URI URL = URI.create(AnkhCloudEnvironment.BASE_URL + "/endpoint/login");

  private final UUID serverId = AnkhCloudEula.serverId();
}
