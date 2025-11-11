package com.example.gesioncompteom.config;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration Twilio pour l'envoi de SMS.
 * 
 * Cette classe initialise le SDK Twilio avec les identifiants
 * stockés dans les variables d'environnement.
 * 
 * Identifiants requis:
 * - TWILIO_ACCOUNT_SID: ID du compte Twilio
 * - TWILIO_AUTH_TOKEN: Token d'authentification Twilio
 * - TWILIO_FROM: Numéro Twilio à partir duquel envoyer les SMS
 */
@Configuration
public class TwilioConfig {

    /**
     * Account SID Twilio (trouvé sur https://console.twilio.com)
     */
    @Value("${TWILIO_SID:}")
    private String accountSid;

    /**
     * Auth Token Twilio (trouvé sur https://console.twilio.com)
     */
    @Value("${TWILIO_AUTH_TOKEN:}")
    private String authToken;

    /**
     * Numéro Twilio à partir duquel envoyer les SMS
     * Format: +country_codephonenumber (ex: +221782917770)
     */
    @Value("${TWILIO_FROM:}")
    private String fromNumber;

    /**
     * Indicateur si Twilio est configuré
     */
    private boolean isConfigured;

    /**
     * Initialise Twilio au démarrage de l'application.
     * 
     * Cette méthode est appelée après la création du bean,
     * une fois que toutes les injections ont été effectuées.
     */
    @PostConstruct
    public void initTwilio() {
        // Vérifier que tous les paramètres sont configurés
        if (accountSid != null && !accountSid.isEmpty() &&
            authToken != null && !authToken.isEmpty() &&
            fromNumber != null && !fromNumber.isEmpty()) {
            
            try {
                // Initialiser le SDK Twilio
                Twilio.init(accountSid, authToken);
                this.isConfigured = true;
                
                System.out.println("✅ Twilio initialisé avec succès");
                System.out.println("   Account SID: " + accountSid.substring(0, 5) + "...");
                System.out.println("   Numéro Twilio: " + fromNumber);
                
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l'initialisation de Twilio: " + e.getMessage());
                this.isConfigured = false;
            }
        } else {
            System.out.println("⚠️  Twilio non configuré - SMS ne seront pas envoyés");
            System.out.println("   TWILIO_ACCOUNT_SID: " + (accountSid != null ? "OK" : "❌ MANQUANT"));
            System.out.println("   TWILIO_AUTH_TOKEN: " + (authToken != null ? "OK" : "❌ MANQUANT"));
            System.out.println("   TWILIO_FROM: " + (fromNumber != null ? "OK" : "❌ MANQUANT"));
            this.isConfigured = false;
        }
    }

    // Getters
    public String getAccountSid() {
        return accountSid;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getFromNumber() {
        return fromNumber;
    }

    public boolean isConfigured() {
        return isConfigured;
    }
}
