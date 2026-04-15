package com.docutrack.service;

public interface WhatsAppNotificationService {
  boolean sendText(String toMobileNumber, String messageText);
}
