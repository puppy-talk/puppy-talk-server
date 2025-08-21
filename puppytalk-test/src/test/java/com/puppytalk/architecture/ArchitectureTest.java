package com.puppytalk.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@DisplayName("레이어 의존성 검증")
class ArchitectureTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
            .importPackages("com.puppytalk");

    @Test
    @DisplayName("API 계층은 Application 계층만 의존한다")
    void apiLayerShouldOnlyDependOnApplicationLayer() {
        ArchRule apiLayerRule = classes()
                .that().resideInAPackage("..api..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..application..",
                    "java..",
                    "javax..",
                    "jakarta..",
                    "org.springframework..",
                    "org.slf4j..",
                    "com.fasterxml.jackson.."
                )
                .allowEmptyShould(true);

        apiLayerRule.check(importedClasses);
    }

    @Test
    @DisplayName("Application 계층은 Domain 계층만 의존한다")
    void applicationLayerShouldOnlyDependOnDomainLayer() {
        ArchRule applicationLayerRule = classes()
                .that().resideInAPackage("..application..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..domain..",
                    "java..",
                    "javax..",
                    "jakarta..",
                    "org.springframework..",
                    "org.slf4j.."
                )
                .allowEmptyShould(true);

        applicationLayerRule.check(importedClasses);
    }

    @Test
    @DisplayName("Infrastructure 계층은 Domain 계층만 의존한다")
    void infrastructureLayerShouldOnlyDependOnDomainLayer() {
        ArchRule infrastructureLayerRule = classes()
                .that().resideInAPackage("..infrastructure..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..domain..",
                    "java..",
                    "javax..",
                    "jakarta..",
                    "org.springframework..",
                    "org.slf4j..",
                    "com.mysql..",
                    "org.h2..",
                    "org.hibernate.."
                )
                .allowEmptyShould(true);

        infrastructureLayerRule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain 계층은 외부에 의존하지 않는다")
    void domainLayerShouldNotDependOnAnyLayer() {
        ArchRule domainLayerRule = classes()
                .that().resideInAPackage("..domain..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..domain..",
                    "java..",
                    "javax..",
                    "jakarta.."
                )
                .allowEmptyShould(true);

        domainLayerRule.check(importedClasses);
    }
}