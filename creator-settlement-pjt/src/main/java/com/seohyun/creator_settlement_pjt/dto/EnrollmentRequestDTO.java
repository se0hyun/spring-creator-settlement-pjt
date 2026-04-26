package com.seohyun.creator_settlement_pjt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@NoArgsConstructor
public class EnrollmentRequestDTO {

    @NotNull
    private Long courseId;

    @NotNull
    private Long studentId;

    @NotNull
    private LocalDateTime paidAt;

}
