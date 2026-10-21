package com.borjaglez.dddlab;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Las reglas de arquitectura, escritas como tests. Si alguien se salta una, el build falla.
 *
 * <p>Los módulos de Gradle ya impiden algunas cosas en compilación (el dominio no puede importar
 * Spring porque no lo tiene en el classpath). Estas reglas cubren lo que el compilador no ve: qué
 * paquete puede usar qué dentro de un mismo módulo, y los límites entre bounded contexts.
 */
@AnalyzeClasses(
    packages = {"com.borjaglez.dddlab", "com.legacypay"},
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  private static final String CATALOG = "com.borjaglez.dddlab.catalog..";
  private static final String ORDERING = "com.borjaglez.dddlab.ordering..";
  private static final String NOTIFICATIONS = "com.borjaglez.dddlab.notifications..";

  // ---------------------------------------------------------------- Hexagonal

  /** La regla de dependencias: las flechas apuntan siempre hacia dentro. */
  @ArchTest
  static final ArchRule dependenciesPointInwards =
      layeredArchitecture()
          .consideringOnlyDependenciesInLayers()
          .layer("Domain")
          .definedBy("com.borjaglez.dddlab.ordering.domain..")
          .layer("Application")
          .definedBy("com.borjaglez.dddlab.ordering.application..")
          .layer("Infrastructure")
          .definedBy("com.borjaglez.dddlab.ordering.infrastructure..")
          .whereLayer("Infrastructure")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Application")
          .mayOnlyBeAccessedByLayers("Infrastructure")
          .whereLayer("Domain")
          .mayOnlyBeAccessedByLayers("Application", "Infrastructure");

  @ArchTest
  static final ArchRule theCoreIsFrameworkFree =
      noClasses()
          .that()
          .resideInAnyPackage(
              "com.borjaglez.dddlab.ordering.domain..",
              "com.borjaglez.dddlab.ordering.application..",
              "com.borjaglez.dddlab.sharedkernel..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..", "jakarta..", "org.hibernate..", "tools.jackson..")
          .because("el núcleo es Java puro: Spring, JPA y Jackson viven en el anillo exterior");

  @ArchTest
  static final ArchRule drivingAdaptersDoNotUseDrivenAdapters =
      noClasses()
          .that()
          .resideInAPackage("com.borjaglez.dddlab.ordering.infrastructure.in..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.borjaglez.dddlab.ordering.infrastructure.out..")
          .because("un controlador no habla con la base de datos: pasa por un puerto de entrada");

  @ArchTest
  static final ArchRule drivenAdaptersDoNotUseDrivingAdapters =
      noClasses()
          .that()
          .resideInAPackage("com.borjaglez.dddlab.ordering.infrastructure.out..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.borjaglez.dddlab.ordering.infrastructure.in..");

  @ArchTest
  static final ArchRule noFieldInjection =
      noFields()
          .should()
          .beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
          .because("las dependencias entran por constructor: son final y fáciles de sustituir");

  // ------------------------------------------------------- DDD: context map

  @ArchTest
  static final ArchRule onlyTheAntiCorruptionLayerKnowsLegacyPay =
      noClasses()
          .that()
          .resideOutsideOfPackages(
              "com.borjaglez.dddlab.ordering.infrastructure.out.payment..", "com.legacypay..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.legacypay..")
          .because("el modelo de LegacyPay no cruza la ACL");

  @ArchTest
  static final ArchRule orderingUsesCatalogOnlyThroughItsOpenHostService =
      noClasses()
          .that()
          .resideInAPackage(ORDERING)
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.borjaglez.dddlab.catalog.internal..")
          .because("Catalog solo publica catalog.api; lo demás es privado");

  @ArchTest
  static final ArchRule onlyTheCatalogAdapterTalksToCatalog =
      noClasses()
          .that()
          .resideInAPackage(ORDERING)
          .and()
          .resideOutsideOfPackage("com.borjaglez.dddlab.ordering.infrastructure.out.catalog..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage(CATALOG)
          .because("el núcleo de Ordering pide productos a su puerto ProductCatalog, no a Catalog");

  @ArchTest
  static final ArchRule theUpstreamDoesNotKnowItsDownstreams =
      noClasses()
          .that()
          .resideInAPackage(CATALOG)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(ORDERING, NOTIFICATIONS);

  @ArchTest
  static final ArchRule notificationsConformsToThePublishedLanguageOnly =
      noClasses()
          .that()
          .resideInAPackage(NOTIFICATIONS)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "com.borjaglez.dddlab.ordering.domain..",
              "com.borjaglez.dddlab.ordering.application..",
              "com.borjaglez.dddlab.ordering.infrastructure..")
          .because("Notifications solo conoce los eventos de integración de ordering.contracts");

  /** Separate Ways: Notifications y Catalog no se integran en absoluto. */
  @ArchTest
  static final ArchRule notificationsAndCatalogGoSeparateWays =
      noClasses()
          .that()
          .resideInAPackage(NOTIFICATIONS)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(CATALOG);

  @ArchTest
  static final ArchRule thePublishedLanguageIsMadeOfJdkTypesOnly =
      classes()
          .that()
          .resideInAPackage("com.borjaglez.dddlab.ordering.contracts..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("java..", "com.borjaglez.dddlab.ordering.contracts..")
          .because("un contrato público no puede arrastrar el modelo interno de Ordering");
}
