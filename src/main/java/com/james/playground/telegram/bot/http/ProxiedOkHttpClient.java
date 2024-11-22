package com.james.playground.telegram.bot.http;

import lombok.AllArgsConstructor;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ProxiedOkHttpClient extends OkHttpClient {
  @Override
  public Call newCall(@NotNull Request request) {
    return super.newCall(request);
  }
}
