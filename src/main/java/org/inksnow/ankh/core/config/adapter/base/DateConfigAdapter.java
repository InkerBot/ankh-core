package org.inksnow.ankh.core.config.adapter.base;

import com.google.gson.internal.JavaVersion;
import com.google.gson.internal.PreJava9DateFormatProvider;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateConfigAdapter implements ConfigTypeAdapter<Date> {
  private final List<DateFormat> dateFormats = new ArrayList<>();

  public DateConfigAdapter() {
    dateFormats.add(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US));
    if (!Locale.getDefault().equals(Locale.US)) {
      dateFormats.add(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT));
    }
    if (JavaVersion.isJava9OrLater()) {
      dateFormats.add(PreJava9DateFormatProvider.getUSDateTimeFormat(DateFormat.DEFAULT, DateFormat.DEFAULT));
    }
  }

  @Override
  public Date read(ConfigSection section) {
    val s = section.asString();
    synchronized (this) {
      for (val dateFormat : dateFormats) {
        try {
          return dateFormat.parse(s);
        } catch (ParseException e) {
          //
        }
      }
    }
    try {
      return ISO8601Utils.parse(s, new ParsePosition(0));
    } catch (ParseException e) {
      throw new RuntimeException("Failed parsing '" + "' as Date. " + section.source());
    }
  }
}
