package org.inksnow.ankh.cloud.endpoint;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public final class LoginResponse {
  private final List<String> message;

  private final Boolean newEnabled;
  private final UUID newServerId;
  private final Map<String, Integer> levelMap;
}
