package com.seohyun.creator_settlement_pjt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CancelRequestDTO {

    @Positive
    private int cancelAmount;

    @NotNull
    private LocalDateTime canceledAt;

}
