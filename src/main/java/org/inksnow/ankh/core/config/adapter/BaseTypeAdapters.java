package org.inksnow.ankh.core.config.adapter;

import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;
import org.inksnow.ankh.core.common.util.UUIDUtil;
import org.inksnow.ankh.core.config.adapter.base.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.*;

import static org.inksnow.ankh.core.config.adapter.AdaptersUtils.createFactory;

@SuppressWarnings("unused")
public final class BaseTypeAdapters {
  public static final ConfigTypeAdapter.Factory<ConfigSection> CONFIG_SECTION = createFactory(ConfigSection.class, it -> it).nullable();
  public static final ConfigTypeAdapter.Factory<Number> NUMBER = createFactory(Number.class, ConfigSection::asNumber).nullable();
  public static final ConfigTypeAdapter.Factory<Integer> INTEGER = createFactory(int.class, Integer.class, ConfigSection::asInteger).nullable();
  public static final ConfigTypeAdapter.Factory<Byte> BYTE = createFactory(byte.class, Byte.class, ConfigSection::asByte).nullable();
  public static final ConfigTypeAdapter.Factory<Character> CHARACTER = createFactory(char.class, Character.class, ConfigSection::asCharacter).nullable();
  public static final ConfigTypeAdapter.Factory<Boolean> BOOLEAN = createFactory(boolean.class, Boolean.class, ConfigSection::asBoolean).nullable();
  public static final ConfigTypeAdapter.Factory<Double> DOUBLE = createFactory(double.class, Double.class, ConfigSection::asDouble).nullable();
  public static final ConfigTypeAdapter.Factory<Float> FLOAT = createFactory(float.class, Float.class, ConfigSection::asFloat).nullable();
  public static final ConfigTypeAdapter.Factory<Long> LONG = createFactory(long.class, Long.class, ConfigSection::asLong).nullable();
  public static final ConfigTypeAdapter.Factory<BigDecimal> BIG_DECIMAL = createFactory(BigDecimal.class, ConfigSection::asBigDecimal).nullable();
  public static final ConfigTypeAdapter.Factory<BigInteger> BIG_INTEGER = createFactory(BigInteger.class, ConfigSection::asBigInteger).nullable();
  public static final ConfigTypeAdapter.Factory<String> STRING = createFactory(String.class, ConfigSection::asString).nullable();
  public static final ConfigTypeAdapter.Factory<AtomicBoolean> ATOMIC_BOOLEAN = createFactory(AtomicBoolean.class, it -> new AtomicBoolean(it.asBoolean())).nullable();
  public static final ConfigTypeAdapter.Factory<AtomicInteger> ATOMIC_INTEGER = createFactory(AtomicInteger.class, it -> new AtomicInteger(it.asInteger())).nullable();
  public static final ConfigTypeAdapter.Factory<AtomicIntegerArray> ATOMIC_INTEGER_ARRAY = createFactory(AtomicIntegerArray.class, it -> {
    if (it.isArray()) {
      val list = it.asList();
      val array = new AtomicIntegerArray(list.size());
      for (int i = 0; i < list.size(); i++) {
        array.set(i, list.get(i).asInteger());
      }
      return array;
    } else {
      throw new IllegalStateException(it.source() + " isn't a array value");
    }
  }).nullable();
  public static final ConfigTypeAdapter.Factory<AtomicLong> ATOMIC_LONG = createFactory(AtomicLong.class, it -> new AtomicLong(it.asLong())).nullable();
  public static final ConfigTypeAdapter.Factory<AtomicLongArray> ATOMIC_LONG_ARRAY = createFactory(AtomicLongArray.class, it -> {
    if (it.isArray()) {
      val list = it.asList();
      val array = new AtomicLongArray(list.size());
      for (int i = 0; i < list.size(); i++) {
        array.set(i, list.get(i).asLong());
      }
      return array;
    } else {
      throw new IllegalStateException(it.source() + " isn't a array value");
    }
  }).nullable();
  public static final ConfigTypeAdapter.Factory<StringBuffer> STRING_BUFFER = createFactory(StringBuffer.class, it -> new StringBuffer(it.asString())).nullable();
  public static final ConfigTypeAdapter.Factory<StringBuilder> STRING_BUILDER = createFactory(StringBuilder.class, it -> new StringBuilder(it.asString())).nullable();
  public static final ConfigTypeAdapter.Factory<URL> URL = createFactory(URL.class, new ConfigTypeAdapter<URL>() {
    @Override
    @SneakyThrows
    public URL read(ConfigSection section) {
      return new URL(section.asString());
    }
  }).nullable();
  public static final ConfigTypeAdapter.Factory<URI> URI = createFactory(URI.class, new ConfigTypeAdapter<URI>() {
    @Override
    @SneakyThrows
    public URI read(ConfigSection section) {
      return new URI(section.asString());
    }
  }).nullable();
  public static final ConfigTypeAdapter.Factory<UUID> UUID = createFactory(UUID.class, it -> UUIDUtil.fromString(it.asString())).nullable();
  public static final ConfigTypeAdapter.Factory<Currency> CURRENCY = createFactory(Currency.class, new ConfigTypeAdapter<Currency>() {
    @Override
    @SneakyThrows
    public Currency read(ConfigSection section) {
      return Currency.getInstance(section.asString());
    }
  }).nullable();
  public static final ConfigTypeAdapter.Factory<Locale> LOCALE = createFactory(Locale.class, new ConfigTypeAdapter<Locale>() {
    @Override
    @SneakyThrows
    public Locale read(ConfigSection section) {
      val locale = section.asString();
      val tokenizer = new StringTokenizer(locale, "_");
      String language = null;
      String country = null;
      String variant = null;
      if (tokenizer.hasMoreElements()) {
        language = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreElements()) {
        country = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreElements()) {
        variant = tokenizer.nextToken();
      }
      if (country == null && variant == null) {
        return new Locale(language);
      } else if (variant == null) {
        return new Locale(language, country);
      } else {
        return new Locale(language, country, variant);
      }
    }
  }).nullable();
  public static final ConfigTypeAdapter.Factory<InetAddress> INET_ADDRESS = createFactory(InetAddress.class, new ConfigTypeAdapter<InetAddress>() {
    @Override
    @SneakyThrows
    public InetAddress read(ConfigSection section) {
      return InetAddress.getByName(section.asString());
    }
  }).nullable();
  public static final ConfigTypeAdapter.Factory<BitSet> BIT_SET = createFactory(BitSet.class, it -> {
    val bitset = new BitSet();
    val list = it.asList();
    for (int i = 0; i < list.size(); i++) {
      val section = list.get(i);
      bitset.set(i, section.asBoolean());
    }
    return bitset;
  }).nullable();
  public static final ConfigTypeAdapter.Factory<Date> DATE = createFactory(Date.class, new DateConfigAdapter()).nullable();
  public static final ConfigTypeAdapter.Factory<Calendar> CALENDAR = createFactory(Calendar.class, GregorianCalendar.class, it -> {
    val yearSection = it.get("year");
    val year = yearSection == null ? 0 : yearSection.asInteger();
    val monthSection = it.get("month");
    val month = monthSection == null ? 0 : monthSection.asInteger();
    val dayOfMonthSection = it.get("dayOfMonth");
    val dayOfMonth = dayOfMonthSection == null ? 0 : dayOfMonthSection.asInteger();
    val hourOfDaySection = it.get("hourOfDay");
    val hourOfDay = hourOfDaySection == null ? 0 : hourOfDaySection.asInteger();
    val minuteSection = it.get("minute");
    val minute = minuteSection == null ? 0 : minuteSection.asInteger();
    val secondSection = it.get("second");
    val second = secondSection == null ? 0 : secondSection.asInteger();
    return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
  }).nullable();
  public static final ConfigTypeAdapter.Factory<Object> ARRAY = new ArrayConfigAdapter.Factory().nullable();
  public static final ConfigTypeAdapter.Factory<Collection<?>> COLLECTION = new CollectionConfigAdapter.Factory().nullable();
  public static final ConfigTypeAdapter.Factory<Map<?, ?>> MAP = new MapConfigAdapter.Factory().nullable();
  public static final ConfigTypeAdapter.Factory<Object> CODEC = new CodecConfigAdapter.Factory().nullable();
  public static final ConfigTypeAdapter.Factory<Enum<?>> ENUM = new EnumConfigAdapter.Factory().nullable();
  public static final ConfigTypeAdapter.Factory<Object> INTERFACE = new InterfaceConfigAdapter.Factory().nullable();
  public static final ConfigTypeAdapter.Factory<Object> RECORD = new RecordConfigAdapter.Factory().nullable();
  public static final ConfigTypeAdapter.Factory<Object> OBJECT = new ObjectConfigAdapter.Factory().nullable();
  public static final ConfigTypeAdapter.Factory<Object> NULL = new ConfigTypeAdapter.Factory<Object>() {
    private final ConfigTypeAdapter<Object> NULL = it -> null;

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ConfigTypeAdapter create(ConfigLoader configLoader, TypeToken typeToken) {
      return NULL;
    }
  };

  private BaseTypeAdapters() {
    throw new UnsupportedOperationException();
  }
}
