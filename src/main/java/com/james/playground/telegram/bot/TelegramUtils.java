package com.james.playground.telegram.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.james.playground.telegram.bot.method.TelegramMethodCallback;
import com.james.playground.temporal.utils.ExceptionUtils;
import com.james.playground.utils.FormatUtils;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.telegram.telegrambots.client.TelegramMultipartBuilder;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TelegramUtils {
  private static final String SET_WEBHOOK_API_PATH = "setWebhook";
  private static final String DELETE_WEBHOOK_API_PATH = "deleteWebhook";
  private static final ContentType TEXT_PLAIN_CONTENT_TYPE = ContentType.create("text/plain", StandardCharsets.UTF_8);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final OkHttpClient CLIENT = (new OkHttpClient.Builder()).build();

  public static void setWebhook(BotConfigs configs, SetWebhook request) {
    try {
      request.validate();

      HttpUrl url = configs.buildUrl(request.getMethod());
      MultipartBody body = buildSetWebhookRequestBody(configs, request);

      Request httpPost = (new Request.Builder()).url(url).post(body).build();

      Boolean result = sendRequest(request, httpPost).get();
      if (!Boolean.TRUE.equals(result)) {
        throw new TelegramApiRequestException("error setting webhook");
      }

    } catch (Exception ex) {
      throw new RuntimeException(ex);

    }
  }

  static MultipartBody buildSetWebhookRequestBody(BotConfigs configs, SetWebhook request) throws JsonProcessingException {
    try {
      return new TelegramMultipartBuilder(FormatUtils.OBJECT_MAPPER)
          .addPart("url", configs.getWebhookUrl(request.getUrl()))
          .addPart("max_connections", request.getMaxConnections())
          .addJsonPart("allowed_updates", request.getAllowedUpdates())
          .addPart("ip_address", request.getIpAddress())
          .addPart("drop_pending_updates", request.getDropPendingUpdates())
          .addPart("secret_token", request.getSecretToken())
          .addInputFile("certificate", request.getCertificate(), true)
          .build();

    } catch (Exception ex) {
      throw new RuntimeException(ex);

    }
  }

  static CloseableHttpClient getCloseableHttpClient() {
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier())
        .setConnectionTimeToLive(70L, TimeUnit.SECONDS)
        .setMaxConnTotal(100);

    return httpClientBuilder.build();
  }

  public static void clearWebhook(BotConfigs configs) {
    try {
      DeleteWebhook request = new DeleteWebhook(true);

      Boolean result = execute(configs, request);
      if (!Boolean.TRUE.equals(result)) {
        throw new TelegramApiRequestException("error clearing webhook");
      }

    } catch (Exception ex) {
      throw new RuntimeException(ex);

    }
  }

  static HttpPost buildClearWebhookRequest(BotConfigs configs, DeleteWebhook request) {
    RequestConfig requestConfig =
        RequestConfig.custom()
            .setSocketTimeout(75000)
            .setConnectTimeout(75000)
            .setConnectionRequestTimeout(75000)
            .build();

    HttpPost httpPost = new HttpPost(configs.getRequestUrl(DELETE_WEBHOOK_API_PATH));
    httpPost.setConfig(requestConfig);
    httpPost.addHeader("charset", StandardCharsets.UTF_8.name());
    httpPost.setEntity(
        new StringEntity(FormatUtils.toJsonString(request), ContentType.APPLICATION_JSON));

    return httpPost;
  }

  static <T extends Serializable, M extends BotApiMethod<T>> T execute(BotConfigs configs, M method) {
    try {
      HttpUrl url = configs.buildUrl(method.getMethod());
      String body = FormatUtils.toJsonString(method);
      Headers headers = (new Headers.Builder()).add("charset", StandardCharsets.UTF_8.name()).add("content-type", "application/json").build();
      Request request = (new Request.Builder()).url(url).headers(headers).post(RequestBody.create(body, MediaType.parse("application/json"))).build();
      return sendRequest(method, request).get();

    } catch (Exception ex) {
      throw new RuntimeException(String.format("failed to invoke %s, error: %s", method.getMethod(), ExceptionUtils.getStackTrace(ex)));
    }
  }

  static <T extends Serializable, M extends PartialBotApiMethod<T>> TelegramMethodCallback<T, M> sendRequest(M method, Request request) {
    TelegramMethodCallback<T, M> callback = new TelegramMethodCallback<>(method);
    CLIENT.newCall(request).enqueue(callback);
    return callback;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BotConfigs {
    private static final String BASE_URL = "https://api.telegram.org/bot";
    private String botUsername;
    private String botToken;
    private String botPath;

    public String getRequestUrl(String apiPath) {
      return BASE_URL + this.botToken + "/" + apiPath;
    }

    public HttpUrl buildUrl(String methodPath) {
      return (new HttpUrl.Builder())
          .scheme(TelegramUrl.DEFAULT_URL.getSchema())
          .host(TelegramUrl.DEFAULT_URL.getHost())
          .port(TelegramUrl.DEFAULT_URL.getPort())
          .addPathSegment("bot" + this.botToken)
          .addPathSegment(methodPath)
          .build();
    }

    public String getWebhookUrl(String baseUrl) {
      if (!baseUrl.endsWith("/")) {
        baseUrl = baseUrl + "/";
      }

      baseUrl = baseUrl + "callback";
      if (StringUtils.isNotEmpty(this.botPath)) {
        if (!this.botPath.startsWith("/")) {
          baseUrl = baseUrl + "/";
        }

        baseUrl = baseUrl + this.botPath;
      }

      return baseUrl;
    }
  }
}
