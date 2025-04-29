package com.kycengine.knowyourcustomer.common;

import java.util.HashMap;
import java.util.Map;

public class CommonUtils
{

  /**
   * Formats the given time in milliseconds into hours, minutes, and seconds.
   *
   * @param expiryMillis The time in milliseconds.
   * @return A map containing the hours, minutes, and seconds.
   */
  public static Map<String, String> formatExpiryTime(long expiryMillis) {
    long hours = (expiryMillis / 1000) / 3600;
    long minutes = ((expiryMillis / 1000) % 3600) / 60;
    long seconds = (expiryMillis / 1000) % 60;

    Map<String, String> formattedExpiry = new HashMap<>();
    formattedExpiry.put("hours", String.valueOf(hours));
    formattedExpiry.put("minutes", String.valueOf(minutes));
    formattedExpiry.put("seconds", String.valueOf(seconds));

    return formattedExpiry;
  }
}
