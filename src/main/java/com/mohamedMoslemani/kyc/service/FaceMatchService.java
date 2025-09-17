package com.mohamedMoslemani.kyc.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Collections;
import java.util.Map;

@Service
public class FaceMatchService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String FACE_MATCH_URL = "http://localhost:5000/face-match";

    public Map<String, Object> verifyFaces(byte[] selfie, byte[] idPhoto) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("selfie", new ByteArrayResource(selfie) {
                @Override
                public String getFilename() {
                    return "selfie.jpg";
                }
            });
            body.add("id_photo", new ByteArrayResource(idPhoto) {
                @Override
                public String getFilename() {
                    return "id.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    FACE_MATCH_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyMap();

        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }
}
