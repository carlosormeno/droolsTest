package com.example.droolsTest.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class DroolsConfig {

    private static final Logger logger = LoggerFactory.getLogger(DroolsConfig.class);

    @Bean
    public KieServices kieServices() {
        return KieServices.Factory.get();
    }

    @Bean
    public KieContainer kieContainer(KieServices kieServices) {
        logger.info("Inicializando KieContainer...");

        KieRepository kieRepository = kieServices.getRepository();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        // Cargar reglas desde el classpath
        loadRulesFromClasspath(kieFileSystem);

        // Construir el módulo
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        // Verificar errores de compilación
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            logger.error("Errores en la compilación de reglas:");
            for (Message message : results.getMessages(Message.Level.ERROR)) {
                logger.error("Error: {}", message.getText());
            }
            throw new RuntimeException("Errores en la compilación de reglas Drools");
        }

        KieModule kieModule = kieBuilder.getKieModule();
        KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());

        logger.info("KieContainer inicializado exitosamente");
        return kieContainer;
    }

    @Bean
    public KieSession kieSession(KieContainer kieContainer) {
        logger.info("Creando KieSession...");
        return kieContainer.newKieSession();
    }

    private void loadRulesFromClasspath(KieFileSystem kieFileSystem) {
        try {
            // Cargar reglas desde el classpath
            String rulesPath = "rules/sample-rules.drl";

            // Intentar cargar desde el classpath
            try {
                kieFileSystem.write(ResourceFactory.newClassPathResource(rulesPath));
                logger.info("Reglas cargadas desde classpath: {}", rulesPath);
            } catch (Exception e) {
                logger.warn("No se pudieron cargar reglas desde classpath: {}", rulesPath);

                // Crear reglas por defecto si no existen
                String defaultRules = createDefaultRules();
                kieFileSystem.write("src/main/resources/rules/default-rules.drl", defaultRules);
                logger.info("Reglas por defecto creadas");
            }

        } catch (Exception e) {
            logger.error("Error al cargar reglas: {}", e.getMessage());

            // Crear reglas por defecto en caso de error
            String defaultRules = createDefaultRules();
            kieFileSystem.write("src/main/resources/rules/default-rules.drl", defaultRules);
            logger.info("Reglas por defecto aplicadas debido a error");
        }
    }

    private String createDefaultRules() {
        return """
            package com.empresa.drools.rules;
            
            import com.empresa.drools.model.Customer;
            
            rule "Cliente Joven - Descuento"
                when
                    $customer : Customer(age < 25)
                then
                    $customer.setDiscount("15% descuento jóvenes");
                    $customer.setRecommendation("¡Aprovecha tu descuento juvenil!");
            end
            
            rule "Cliente VIP - Clasificación"
                when
                    $customer : Customer(totalPurchases > 5000)
                then
                    $customer.setCategory("VIP");
                    $customer.setDiscount("20% descuento VIP");
                    $customer.setRecommendation("Acceso a productos premium");
            end
            
            rule "Cliente Nuevo - Bienvenida"
                when
                    $customer : Customer(isNewCustomer() == true)
                then
                    $customer.setRecommendation("¡Bienvenido! Conoce nuestros productos");
                    $customer.setEligibleForPromotion(true);
            end
            """;
    }

    /**
     * Método para recargar reglas dinámicamente (útil para el servicio)
     */
    public KieContainer reloadRules(KieServices kieServices, List<String> ruleContents) {
        logger.info("Recargando reglas dinámicamente...");

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        // Agregar cada regla
        for (int i = 0; i < ruleContents.size(); i++) {
            String rulePath = "src/main/resources/rules/dynamic-rule-" + i + ".drl";
            String fullRuleContent = "package com.empresa.drools.rules;\n" +
                    "import com.empresa.drools.model.Customer;\n\n" +
                    ruleContents.get(i);
            kieFileSystem.write(rulePath, fullRuleContent);
        }

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            logger.error("Errores al recargar reglas:");
            for (Message message : results.getMessages(Message.Level.ERROR)) {
                logger.error("Error: {}", message.getText());
            }
            throw new RuntimeException("Errores al recargar reglas Drools");
        }

        KieModule kieModule = kieBuilder.getKieModule();
        KieContainer newContainer = kieServices.newKieContainer(kieModule.getReleaseId());

        logger.info("Reglas recargadas exitosamente");
        return newContainer;
    }
}
