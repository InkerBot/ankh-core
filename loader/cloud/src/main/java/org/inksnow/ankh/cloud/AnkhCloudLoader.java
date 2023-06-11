package org.inksnow.ankh.cloud;

import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.bukkit.plugin.Plugin;
import org.inksnow.ankh.cloud.bean.LogBean;
import org.inksnow.ankh.cloud.endpoint.*;
import org.inksnow.ankh.cloud.util.PluginSchedulerExecutor;

import java.io.*;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AnkhCloudLoader {
  private static final Logger logger = Logger.getLogger("ankh-cloud");
  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
  private static final Gson GSON = new GsonBuilder().create();
  private static final ScheduledThreadPoolExecutor scheduledPool = provideScheduledThreadPool();
  private static final HttpClient httpClient = HttpClients.custom()
      .setUserAgent("AnkhCloud/1.0.0(im@inker.bot)")
      .build();
  private static final Object logQueueLock = new Object();
  @Getter
  private static final Map<String, Integer> levelMap = new LinkedHashMap<>();
  @Getter
  private static final boolean enabled = handleLogin();
  private static List<LogBean> logQueue = new LinkedList<>();

  private AnkhCloudLoader() {
    throw new UnsupportedOperationException();
  }

  private static ScheduledThreadPoolExecutor provideScheduledThreadPool() {
    val uncaughtExceptionHandler = (Thread.UncaughtExceptionHandler) (t, e) -> {
      logger.log(Level.SEVERE, "Uncaught exception in thread \"" + t.getName() + "\" ", e);
    };
    val threadFactory = new ThreadFactoryBuilder()
        .setNameFormat("ankh-cloud-%d")
        .setDaemon(true)
        .setUncaughtExceptionHandler(uncaughtExceptionHandler)
        .build();
    return new ScheduledThreadPoolExecutor(4, threadFactory);
  }

  private static boolean handleLogin() {
    try {
      AnkhCloudEula.init();
      if (!AnkhCloudEula.enabled()) {
        return false;
      }
      val response = new LoginResponse[1];
      runWithTimeReport("login-ankh-cloud", () -> {
        response[0] = sendPostJson(LoginRequest.URL, LoginRequest.builder().build(), LoginResponse.class);
      });
      for (String message : response[0].message()) {
        logger.info(message);
      }
      applyResponse(response[0]);
      return AnkhCloudEula.enabled();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed to login ankh cloud", e);
      return false;
    }
  }

  public static void initial() {
    scheduledPool.scheduleAtFixedRate(safeInvokable(() -> {
      List<LogBean> copyLogQueue;
      synchronized (logQueueLock) {
        copyLogQueue = logQueue;
        if (copyLogQueue.isEmpty()) {
          return;
        }
        logQueue = new LinkedList<>();
      }
      runWithRetry(
          () -> postLogBean(copyLogQueue),
          scheduledPool,
          e -> logger.log(Level.WARNING, "Failed to upload log to ankh cloud", e)
      );
    }), 0, 3, TimeUnit.SECONDS);
  }

  @SneakyThrows
  public static void load(Plugin plugin) {
    val pluginExecutor = PluginSchedulerExecutor.of(plugin);

    boolean isSystemReportSuccess = false;
    try {
      runWithTimeReport("post-system-report", () -> postSystemReport(pluginExecutor));
      isSystemReportSuccess = true;
    } catch (Exception e) {
      logger.log(Level.WARNING, "Failed to post ankh cloud system report", e);
    }

    scheduledPool.scheduleAtFixedRate(safeInvokable(() -> {
      runWithRetry(
          () -> postSystemReport(pluginExecutor),
          scheduledPool,
          e -> logger.log(Level.WARNING, "Failed to post system report to ankh cloud", e)
      );
    }), isSystemReportSuccess ? 1 : 0, 1, TimeUnit.HOURS);

    scheduledPool.scheduleAtFixedRate(safeInvokable(() -> {
      runWithRetry(
          () -> postHeartBeat(pluginExecutor),
          scheduledPool,
          e -> logger.log(Level.WARNING, "Failed to post heartbeat to ankh cloud", e)
      );
    }), 0, 1, TimeUnit.MINUTES);
  }

  public static void submitLogBean(LogBean bean) {
    synchronized (logQueueLock) {
      logQueue.add(bean);
    }
  }

  private static void postLogBean(List<LogBean> beanList) throws IOException {
    val request = PostLogRequest.builder()
        .entries(beanList)
        .build();
    sendPostJson(PostLogRequest.URL, request, null);
  }

  private static void postHeartBeat(Executor primaryThreadExecutor) throws IOException {
    val request = HeartRequest.create(primaryThreadExecutor);
    sendPostJson(HeartRequest.URL, request, null);
  }

  private static void postSystemReport(Executor primaryThreadExecutor) throws IOException {
    val request = CompletableFuture.supplyAsync(SystemReportRequest::create, primaryThreadExecutor)
        .join();
    sendPostJson(SystemReportRequest.URL, request, null);
  }

  private static void runWithRetry(Invokable<?> action, Executor retryExecutor, Consumer<Throwable> failureHandle) {
    Runnable[] implRunnable = new Runnable[1];
    int[] retry = new int[]{3};
    implRunnable[0] = () -> {
      try {
        action.run();
      } catch (Throwable e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        } else {
          if ((--retry[0]) > 0) {
            retryExecutor.execute(implRunnable[0]);
          } else {
            failureHandle.accept(e);
          }
        }
      }
    };
    implRunnable[0].run();
  }

  private static Runnable safeInvokable(Invokable<?> action) {
    return () -> {
      try {
        action.run();
      } catch (Throwable e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        } else {
          val wrapException = new IllegalStateException("safe uncheck invokable throws exception", e);
          logger.log(Level.SEVERE, "safe uncheck invokable throws exception", wrapException);
        }
      }
    };
  }

  private static <T extends Throwable> void runWithTimeReport(String title, Invokable<T> action) throws T {
    val startTime = System.nanoTime();
    action.run();
    val passTime = System.nanoTime() - startTime;
    logger.info("finished " + title + " in " + TimeUnit.NANOSECONDS.toMillis(passTime) + "ms");
  }

  private static Runnable uncheckInvokable(Invokable<?> action) {
    return new Runnable() {
      @Override
      @SneakyThrows
      public void run() {
        action.run();
      }
    };
  }

  private static void applyResponse(LoginResponse loginResponse) throws IOException {
    boolean modified = false;
    if (loginResponse.newEnabled() != null) {
      AnkhCloudEula.enabled(loginResponse.newEnabled());
      modified = true;
    }
    if (loginResponse.newServerId() != null) {
      AnkhCloudEula.serverId(loginResponse.newServerId());
      modified = true;
    }
    if (modified) {
      AnkhCloudEula.save();
    }
    levelMap.putAll(loginResponse.levelMap());
  }

  private static <T> T sendPostJson(URI url, Object data, Class<T> responseType) throws IOException {
    byte[] contentBytes = encodeContent(data);

    val request = new HttpPost(url);
    request.setEntity(new ByteArrayEntity(contentBytes));
    request.setHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
    val response = httpClient.execute(request);
    val statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != 200 && statusCode != 204) {
      String content = "";
      try (val contentInput = response.getEntity().getContent()) {
        content = new String(ByteStreams.toByteArray(contentInput), StandardCharsets.UTF_8);
      } catch (Exception e) {
        //
      }
      throw new IOException("AnkhCloud return non-2xx response: " + content);
    }
    if (response.getEntity() != null) {
      try (val contentInput = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8)) {
        return responseType == null ? null : GSON.fromJson(contentInput, responseType);
      }
    } else {
      return null;
    }
  }

  private static void safeCloseConnection(URLConnection connection) {
    try {
      connection.getOutputStream().close();
    } catch (Exception e) {
      //
    }
    try {
      connection.getInputStream().close();
    } catch (Exception e) {
      //
    }
  }

  private static byte[] encodeContent(Object data) {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    try (Writer writer = new OutputStreamWriter(bout, StandardCharsets.UTF_8)) {
      GSON.toJson(data, writer);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return bout.toByteArray();
  }

  @FunctionalInterface
  private interface Invokable<T extends Throwable> {
    void run() throws T;
  }
}
