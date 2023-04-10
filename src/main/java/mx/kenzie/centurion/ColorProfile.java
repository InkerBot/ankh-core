package mx.kenzie.centurion;

import lombok.Data;
import net.kyori.adventure.text.format.TextColor;

@Data
public final class ColorProfile {
  private final TextColor light;
  private final TextColor dark;
  private final TextColor highlight;
  private final TextColor pop;
}
