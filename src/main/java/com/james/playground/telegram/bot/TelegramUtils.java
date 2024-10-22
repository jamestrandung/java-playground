package com.james.playground.telegram.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TelegramUtils {
  private static final String SET_WEBHOOK_API_PATH = "setWebhook";
  private static final ContentType TEXT_PLAIN_CONTENT_TYPE = ContentType.create("text/plain", StandardCharsets.UTF_8);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static void setWebhook(BotConfigs configs, SetWebhook request) throws TelegramApiException {
    request.validate();

    try (CloseableHttpClient httpClient = getCloseableHttpClient()) {
      RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(75000).setConnectTimeout(75000).setConnectionRequestTimeout(75000)
          .build();

      HttpPost httpPost = new HttpPost(configs.getRequestUrl(SET_WEBHOOK_API_PATH));
      httpPost.setConfig(requestConfig);

      HttpEntity multipart = buildSetWebhookRequestBody(configs, request);
      httpPost.setEntity(multipart);

      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        String responseContent = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        Boolean result = request.deserializeResponse(responseContent);
        if (!result) {
          throw new TelegramApiRequestException("Error setting webhook:" + responseContent);
        }
      }

    } catch (IOException e) {
      throw new RuntimeException(e);

    }
  }

  static HttpEntity buildSetWebhookRequestBody(BotConfigs configs, SetWebhook request) throws JsonProcessingException {
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    builder.addTextBody("url", configs.getWebhookUrl(request.getUrl()), TEXT_PLAIN_CONTENT_TYPE);

    if (request.getMaxConnections() != null) {
      builder.addTextBody("max_connections", request.getMaxConnections().toString(), TEXT_PLAIN_CONTENT_TYPE);
    }

    if (request.getAllowedUpdates() != null) {
      builder.addTextBody("allowed_updates", OBJECT_MAPPER.writeValueAsString(request.getAllowedUpdates()), TEXT_PLAIN_CONTENT_TYPE);
    }

    if (request.getIpAddress() != null) {
      builder.addTextBody("ip_address", request.getIpAddress(), TEXT_PLAIN_CONTENT_TYPE);
    }

    if (request.getDropPendingUpdates() != null) {
      builder.addTextBody("drop_pending_updates", request.getDropPendingUpdates().toString(), TEXT_PLAIN_CONTENT_TYPE);
    }

    if (request.getSecretToken() != null) {
      builder.addTextBody("secret_token", request.getSecretToken(), TEXT_PLAIN_CONTENT_TYPE);
    }

    if (request.getCertificate() != null) {
      InputFile webhookFile = request.getCertificate();

      if (webhookFile.getNewMediaFile() != null) {
        builder.addBinaryBody("certificate", webhookFile.getNewMediaFile(), ContentType.TEXT_PLAIN, webhookFile.getMediaName());
      } else if (webhookFile.getNewMediaStream() != null) {
        builder.addBinaryBody("certificate", webhookFile.getNewMediaStream(), ContentType.TEXT_PLAIN, webhookFile.getMediaName());
      }
    }

    return builder.build();
  }

  static CloseableHttpClient getCloseableHttpClient() {
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier())
        .setConnectionTimeToLive(70L, TimeUnit.SECONDS)
        .setMaxConnTotal(100);

    return httpClientBuilder.build();
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BotConfigs {
    private static final String BASE_URL = "https://api.telegram.org/bot";
    private String botToken;
    private String botPath;
    private String secretToken;

    public String getRequestUrl(String apiPath) {
      return BASE_URL + this.botToken + "/" + apiPath;
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
