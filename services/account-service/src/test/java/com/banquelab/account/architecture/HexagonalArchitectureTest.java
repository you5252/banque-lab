package com.banquelab.account.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Ces tests ÉCHOUENT si quelqu'un casse les règles de l'architecture hexagonale.
 * C'est comme un garde-fou automatique pendant la revue de code.
 */
@AnalyzeClasses(packages = "com.banquelab.account", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule le_domaine_ne_depend_d_aucun_framework =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..", "jakarta..", "org.hibernate..");

    @ArchTest
    static final ArchRule le_domaine_ne_connait_ni_application_ni_adaptateurs =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application..", "..adapter..");

    @ArchTest
    static final ArchRule l_application_ne_connait_pas_les_adaptateurs =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter..");

    @ArchTest
    static final ArchRule les_adaptateurs_ne_se_connaissent_pas_entre_eux =
            noClasses().that().resideInAPackage("..adapter.in..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter.out..");
}
