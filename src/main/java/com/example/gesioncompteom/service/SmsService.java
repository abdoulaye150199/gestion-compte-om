package com.example.gesioncompteom.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;

    public SmsService() {
        this.accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        this.authToken = System.getenv("TWILIO_AUTH_TOKEN");
        this.fromNumber = System.getenv("TWILIO_FROM");
        if (accountSid != null && authToken != null) {
            Twilio.init(accountSid, authToken);
        }
    }

    public void sendSms(String to, String body) {
        if (accountSid == null || authToken == null || fromNumber == null) {
            // Not configured - log and skip sending in non-prod environments
            System.out.println("Twilio not configured, skipping SMS to " + to + ": " + body);
            return;
        }
        Message.creator(new PhoneNumber(to), new PhoneNumber(fromNumber), body).create();
    }
}
