package org.inksnow.ankh.cloud.bean;

import lombok.*;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public final class PlayerBean {
  public static final String UNKNOWN_ADDRESS = "(unknown)";

  private final UUID uid;
  private final String name;
  private final String address;

  public static PlayerBean fromInstance(Player player) {
    return builder()
        .uid(player.getUniqueId())
        .name(player.getName())
        .address(safeAddress(player.getAddress()))
        .build();
  }

  private static String safeAddress(InetSocketAddress socketAddress) {
    if (socketAddress == null) {
      return UNKNOWN_ADDRESS;
    }
    val address = socketAddress.getAddress();
    if (address == null) {
      return UNKNOWN_ADDRESS;
    }
    val addressString = address.getHostAddress();
    if (addressString == null) {
      return UNKNOWN_ADDRESS;
    }
    val indexOfPercent = addressString.indexOf('%');
    if (indexOfPercent == -1) {
      return addressString;
    } else {
      return addressString.substring(0, indexOfPercent);
    }
  }
}
