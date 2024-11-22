package com.james.playground.telegram.bot.method;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@AllArgsConstructor
public class TelegramMethodCallback<T extends Serializable, M extends PartialBotApiMethod<T>> extends CompletableFuture<T> implements Callback {
  private final M method;

  public void onFailure(@NotNull Call call, @NotNull IOException exception) {
    Objects.requireNonNull(call);
    Objects.requireNonNull(exception);

    this.completeExceptionally(exception);
  }

  public void onResponse(@NonNull Call call, @NonNull Response response) {
    Objects.requireNonNull(call);
    Objects.requireNonNull(response);

    ResponseBody body = response.body();
    if (body == null) {
      this.completeExceptionally(new TelegramApiException("Telegram API returned empty response"));
      return;
    }

    try (body) {
      this.complete(this.method.deserializeResponse(body.string()));

    } catch (Exception ex) {
      this.completeExceptionally(ex);
    }
  }
}
