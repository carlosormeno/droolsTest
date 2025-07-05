package com.example.droolsTest.entity.DTO;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametricaMetadata {

    private String tableName;
    private String className;
    private String packageName;
    private String description;
    private List<String> fields;
    private Map<String, String> fieldDescriptions;
    private Integer totalRegistros;
    private List<Integer> aniosDisponibles;
    private Map<String, Object> metadataAdicional;

}
