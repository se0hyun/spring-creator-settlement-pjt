package com.seohyun.creator_settlement_pjt.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    /**
     * 비즈니스 흐름: 수강(구매·취소) → 판매 내역 조회 → 정산
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Creator Settlement API")
                        .description("크리에이터 정산 관리 시스템 API")
                        .version("v1.0.0"))
                .tags(List.of(
                        new Tag().name("Enrollment")
                                .description("(수강생) 강의 구매·취소"),
                        new Tag().name("SaleRecord")
                                .description("(크리에이터) 판매 내역 조회"),
                        new Tag().name("Settlement")
                                .description("(관리자·크리에이터) 정산 생성·조회·집계")
                ));
    }
}
