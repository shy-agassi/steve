/*
 * SteVe - SteckdosenVerwaltung - https://github.com/RWTH-i5-IDSG/steve
 * Copyright (C) 2013-2020 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group (IDSG).
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.service;

import com.google.common.base.Strings;
import de.rwth.idsg.steve.NotificationFeature;
import de.rwth.idsg.steve.repository.dto.InsertTransactionParams;
import de.rwth.idsg.steve.repository.dto.MailSettings;
import de.rwth.idsg.steve.repository.dto.UpdateTransactionParams;
import lombok.extern.slf4j.Slf4j;
import ocpp.cs._2015._10.RegistrationStatus;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.rwth.idsg.steve.NotificationFeature.OcppStationBooted;
import static de.rwth.idsg.steve.NotificationFeature.OcppStationStatusFailure;
import static de.rwth.idsg.steve.NotificationFeature.OcppStationWebSocketConnected;
import static de.rwth.idsg.steve.NotificationFeature.OcppStationWebSocketDisconnected;
import static de.rwth.idsg.steve.NotificationFeature.OcppTransactionStarted;
import static de.rwth.idsg.steve.NotificationFeature.OcppTransactionEnded;
import static java.lang.String.format;

// Added by Anirudh on 23/03/2021
import org.json.JSONObject;

// import ocpp.cs._2015._10.MeterValue;
// import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 22.01.2016
 */
@Slf4j
@Service
public class NotificationService {

    @Autowired private MailService mailService;
    @Autowired private WebhookService webhookService;

    public void ocppStationBooted(String chargeBoxId, Optional<RegistrationStatus> status) {
        if (isDisabled(OcppStationBooted)) {
            return;
        }

        // String subject = format("Received boot notification from '%s'", chargeBoxId);
        String body;
        String registration_status = "";
        if (status.isPresent()) {
            registration_status = status.get().value();
            body = format("Charging station '%s' is in database and has registration status '%s'.", chargeBoxId, registration_status);
        } else {
            body = format("Charging station '%s' is NOT in database", chargeBoxId);
        }

        JSONObject bodyObject = new JSONObject();
        bodyObject.put("packetType", "boot");
        bodyObject.put("error", "");
        bodyObject.put("connection", "");
        bodyObject.put("connectorId", "0");
        bodyObject.put("chargeBoxId", chargeBoxId);
        bodyObject.put("registration", registration_status);
        bodyObject.put("iat", DateTime.now().toString());
        webhookService.sendAsync("station", bodyObject);

        // mailService.sendAsync(subject, addTimestamp(body));
    }

    public void ocppHeartbeat(String chargeBoxId, String timestamp) {}

    public void ocppDiagnostics(String chargeBoxId, String status) {}

    // public void ocppMetering(String chargeBoxId, String connectorId, String transactionId, List<MeterValue> meterValue) {}

    public void ocppStationWebSocketConnected(String chargeBoxId) {
        if (isDisabled(OcppStationWebSocketConnected)) {
            return;
        }

        // String subject = format("Connected to JSON charging station '%s'", chargeBoxId);

        JSONObject bodyObject = new JSONObject();
        bodyObject.put("packetType", "wsconnect");
        bodyObject.put("error", "");
        bodyObject.put("registration", "");
        bodyObject.put("connection", "true");
        bodyObject.put("connectorId", "0");
        bodyObject.put("chargeBoxId", chargeBoxId);
        bodyObject.put("iat", DateTime.now().toString());
        webhookService.sendAsync("station", bodyObject);

        // mailService.sendAsync(subject, addTimestamp(""));
    }

    public void ocppStationWebSocketDisconnected(String chargeBoxId) {
        if (isDisabled(OcppStationWebSocketDisconnected)) {
            return;
        }

        // String subject = format("Disconnected from JSON charging station '%s'", chargeBoxId);

        JSONObject bodyObject = new JSONObject();
        bodyObject.put("packetType", "wsconnect");
        bodyObject.put("error", "");
        bodyObject.put("registration", "");
        bodyObject.put("connectorId", "0");
        bodyObject.put("chargeBoxId", chargeBoxId);
        bodyObject.put("connection", "false");
        bodyObject.put("iat", DateTime.now().toString());
        webhookService.sendAsync("station", bodyObject);

        // mailService.sendAsync(subject, addTimestamp(""));
    }

    public void ocppStationStatusFailure(String chargeBoxId, int connectorId, String errorCode) {
        if (isDisabled(OcppStationStatusFailure)) {
            return;
        }

        // String subject = format("Connector '%s' of charging station '%s' is FAULTED", connectorId, chargeBoxId);
        // String body = format("Status Error Code: '%s'", errorCode);

        JSONObject bodyObject = new JSONObject();
        bodyObject.put("packetType", "fault");
        bodyObject.put("error", errorCode);
        bodyObject.put("registration", "");
        bodyObject.put("connectorId", format("%s", connectorId));
        bodyObject.put("chargeBoxId", chargeBoxId);
        bodyObject.put("connection", "");
        bodyObject.put("iat", DateTime.now().toString());
        webhookService.sendAsync("station", bodyObject);

        // mailService.sendAsync(subject, addTimestamp(body));
    }

    public void ocppTransactionStarted(int transactionId, InsertTransactionParams params) {
        if (isDisabled(OcppTransactionStarted)) {
            return;
        }

        // String subject = format("Transaction '%s' has started on charging station '%s' on connector '%s'", transactionId, params.getChargeBoxId(), params.getConnectorId());

        JSONObject bodyObject = new JSONObject();
        bodyObject.put("chargeBoxId", params.getChargeBoxId());
        bodyObject.put("connectorId", params.getConnectorId());
        bodyObject.put("transactionId", format("%s", transactionId));
        bodyObject.put("transaction", "start");
        bodyObject.put("tag", params.getIdTag());
        bodyObject.put("meter", params.getStartMeterValue());
        bodyObject.put("iat", params.getStartTimestamp().toString());
        webhookService.sendAsync("transaction", bodyObject);

        // mailService.sendAsync(subject, addTimestamp(createContent(params)));
    }

    public void ocppTransactionEnded(UpdateTransactionParams params) {
       if (isDisabled(OcppTransactionEnded)) {
            return;
        }

        // String subject = format("Transaction '%s' has ended on charging station '%s'", params.getTransactionId(), params.getChargeBoxId());

        JSONObject bodyObject = new JSONObject();
        bodyObject.put("chargeBoxId", params.getChargeBoxId());
        bodyObject.put("transactionId", params.getTransactionId());
        bodyObject.put("transaction", "stop");
        bodyObject.put("iat", params.getStopTimestamp().toString());
        bodyObject.put("reason", params.getStopReason());
        bodyObject.put("meter", params.getStopMeterValue());
        webhookService.sendAsync("transaction", bodyObject);

        // mailService.sendAsync(subject, addTimestamp(createContent(params)));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------


    private static String createContent(InsertTransactionParams params) {
        StringBuilder sb = new StringBuilder("Details:").append(System.lineSeparator())
            .append("- chargeBoxId: ").append(params.getChargeBoxId()).append(System.lineSeparator())
            .append("- connectorId: ").append(params.getConnectorId()).append(System.lineSeparator())
            .append("- idTag: ").append(params.getIdTag()).append(System.lineSeparator())
            .append("- startTimestamp: ").append(params.getStartTimestamp()).append(System.lineSeparator())
            .append("- startMeterValue: ").append(params.getStartMeterValue());

        if (params.isSetReservationId()) {
            sb.append(System.lineSeparator()).append("- reservationId: ").append(params.getReservationId());
        }

        return sb.toString();
    }

    private static String createContent(UpdateTransactionParams params) {
        return new StringBuilder("Details:").append(System.lineSeparator())
            .append("- chargeBoxId: ").append(params.getChargeBoxId()).append(System.lineSeparator())
            .append("- transactionId: ").append(params.getTransactionId()).append(System.lineSeparator())
            .append("- stopTimestamp: ").append(params.getStopTimestamp()).append(System.lineSeparator())
            .append("- stopMeterValue: ").append(params.getStopMeterValue()).append(System.lineSeparator())
            .append("- stopReason: ").append(params.getStopReason())
            .toString();
    }


    private boolean isDisabled(NotificationFeature f) {
        MailSettings settings = mailService.getSettings();

        boolean isEnabled = settings.isEnabled()
                && settings.getEnabledFeatures().contains(f)
                && !settings.getRecipients().isEmpty();

        return !isEnabled;
    }

    private static String addTimestamp(String body) {
        String eventTs = "Timestamp of the event: " + DateTime.now().toString();
        String newLine = System.lineSeparator() + System.lineSeparator();

        if (Strings.isNullOrEmpty(body)) {
            return eventTs;
        } else {
            return body + newLine + "--" + newLine + eventTs;
        }
    }

}
