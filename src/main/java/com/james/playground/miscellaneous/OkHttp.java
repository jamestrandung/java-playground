package com.james.playground.miscellaneous;

import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;

public class OkHttp {
  public static void main(String[] args) {
    HttpUrl url = new HttpUrl.Builder()
        .scheme("https")
        .host("www.google.com")
        .addPathSegment("search")
        .addQueryParameter("q", "polar bears")
        .build();

    System.out.printf("%s://%s:%s%n", url.scheme(), url.host(), url.port());

    System.out.println(url);
    System.out.println(url.encodedPath());
    System.out.println(url.encodedQuery());
    System.out.println(url.encodedFragment());
    System.out.println(StringUtils.removeEnd(url.toString(), url.encodedPath() + url.encodedQuery()));
  }
}
