package org.inksnow.ankh.core.common.benchmark;

import org.inksnow.ankh.core.api.util.DcLazy;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.Consumer;

public class BenchmarkRecord {
  private static final double[] REPORTS = new double[]{1, 0.999, 0.995, 0.99, 0.50};
  private final long[] records;
  private final int offset;
  private final long[] sortedRecord;
  private final int sortedOffset;
  private final int length;
  private final DcLazy<BigInteger> count = DcLazy.of(this::createCount);
  private final DcLazy<Long> avg = DcLazy.of(this::createAvg);
  private final DcLazy<Long> max = DcLazy.of(this::createMax);
  private final DcLazy<Long> min = DcLazy.of(this::createMin);

  private BenchmarkRecord(
      final long[] records, final int offset,
      final long[] sortedRecord, final int sortedOffset,
      final int length
  ) {
    this.records = records;
    this.offset = offset;
    this.length = length;
    this.sortedRecord = sortedRecord;
    this.sortedOffset = sortedOffset;
  }

  public static Builder builder(final int size) {
    return new Builder(size);
  }

  public int length() {
    return length;
  }

  public BenchmarkRecord subRecord(final double percent) {
    if (percent > 0) {
      int percentInclude = Math.min(length, (int) (length * percent));
      return subRecord(0, percentInclude);
    } else {
      int percentInclude = Math.min(length, (int) (length * (-percent)));
      return subRecord(percentInclude, length - percentInclude);
    }
  }

  public BenchmarkRecord subRecord(final int offset, final int length) {
    return new BenchmarkRecord(
        sortedRecord, sortedOffset + offset,
        sortedRecord, sortedOffset + offset,
        length
    );
  }

  private BigInteger createCount() {
    BigInteger result = BigInteger.ZERO;
    for (int i = offset; i < offset + length; i++) {
      result = result.add(BigInteger.valueOf(records[i]));
    }
    return result;
  }

  public BigInteger count() {
    return count.get();
  }

  private long createAvg() {
    return count.get().divide(BigInteger.valueOf(length)).longValue();
  }

  public long avg() {
    return avg.get();
  }

  private long createMax() {
    long currentMax = Long.MIN_VALUE;
    for (int i = offset; i < offset + length; i++) {
      if (records[i] > currentMax) {
        currentMax = records[i];
      }
    }
    return currentMax;
  }

  public long max() {
    return max.get();
  }

  private long createMin() {
    long currentMin = Long.MAX_VALUE;
    for (int i = offset; i < offset + length; i++) {
      if (records[i] < currentMin) {
        currentMin = records[i];
      }
    }
    return currentMin;
  }

  public long min() {
    return min.get();
  }

  public void runReport(Consumer<String> output) {
    for (double report : REPORTS) {
      BenchmarkRecord subTopRecord = subRecord(report);
      BenchmarkRecord subLowRecord = subRecord(-report);
      output.accept(report * 100 + "% max(" + subTopRecord.max() + " ns) min(" + subLowRecord.min() + " ns) avg(" + subTopRecord.avg() + " ns)");
    }
  }

  public static class Builder {
    private final int size;
    private final long[] records;
    private int current = 0;

    private Builder(final int size) {
      this.size = size;
      records = new long[size];
    }

    public Builder record(final long time) {
      this.records[current++] = time;
      return this;
    }

    public BenchmarkRecord build() {
      final long[] sortedRecord = Arrays.copyOf(records, current);
      Arrays.sort(sortedRecord);
      return new BenchmarkRecord(records, 0, sortedRecord, 0, current);
    }
  }
}
