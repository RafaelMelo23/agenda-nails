package com.rafael.nailspro.webapp.infrastructure.controller.api.evolution.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafael.nailspro.webapp.application.whatsapp.webhook.WebhookProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhook")
public class WebhookController {

    private final WebhookProcessorService  webhookProcessorService;
    private final ObjectMapper objectMapper;

//    @PostMapping
//    public ResponseEntity<Void> process(@RequestBody EvolutionWebhookResponse<?> dto) {
//
//        webhookProcessorService.handleWebhook(dto);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONNECTION, "close")
//                .build();
//    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<JsonNode> prettyPrintWebhook(@RequestBody JsonNode payload) {

        log.info("EVOLUTION_WEBHOOK_PAYLOAD {}", payload);

        return ResponseEntity.ok(payload);
    }
}