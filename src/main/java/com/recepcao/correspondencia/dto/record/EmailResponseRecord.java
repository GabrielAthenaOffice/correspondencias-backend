package com.recepcao.correspondencia.dto.record;

import java.time.LocalDateTime;

public record EmailResponseRecord(String status, String enviadoPara, LocalDateTime data) {
}
