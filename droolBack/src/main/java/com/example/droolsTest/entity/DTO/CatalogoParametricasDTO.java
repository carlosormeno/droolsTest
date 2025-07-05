package com.example.droolsTest.entity.DTO;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoParametricasDTO {

    private Map<String, ParametricaMetadata> parametricas;
    private Integer totalParametricas;
    private LocalDateTime fechaGeneracion;
    private String version;

}
