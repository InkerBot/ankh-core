package org.inksnow.ankh.cloud.bean;

import lombok.*;

import java.lang.management.MemoryUsage;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public final class MemoryUsageBean {
  private final long init;
  private final long used;
  private final long committed;
  private final long max;

  public static MemoryUsageBean fromInstance(MemoryUsage usage) {
    return builder()
        .init(usage.getInit())
        .used(usage.getUsed())
        .committed(usage.getCommitted())
        .max(usage.getMax())
        .build();
  }
}
