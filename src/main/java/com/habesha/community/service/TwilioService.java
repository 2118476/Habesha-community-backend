package com.habesha.community.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Encapsulates interactions with the Twilio API.  Credentials and
 * configuration are loaded from application properties.  If the
 * credentials are placeholders (the default values) then SMS
 * sending is effectively disabled.  To enable SMS, override the
 * TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN and TWILIO_PHONE_NUMBER
 * environment variables.
 */
@Service
@Slf4j
public class TwilioService {
    @Value("${twilio.accountSid}")
    private String accountSid;
    @Value("${twilio.authToken}")
    private String authToken;
    @Value("${twilio.phoneNumber}")
    private String fromNumber;

    private boolean enabled;

    @PostConstruct
    public void init() {
        // If the credentials are not set, disable SMS
        enabled = !"your_account_sid".equals(accountSid) && !"your_auth_token".equals(authToken);
        if (enabled) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialised successfully");
        } else {
            log.warn("Twilio credentials not configured; SMS sending is disabled");
        }
    }

    public void sendSms(String toNumber, String body) {
        if (!enabled) {
            log.info("SMS not sent because Twilio is disabled. Message: {}", body);
            return;
        }
        try {
            Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    body
            ).create();
            log.info("SMS sent to {}", toNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toNumber, e.getMessage());
        }
    }
}