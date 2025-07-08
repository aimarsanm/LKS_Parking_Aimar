# JaCoCo y SonarQube - Configuración para LKS_Parking_Aimar

Este documento explica cómo está configurado JaCoCo para la cobertura de código y SonarQube para el análisis de calidad de código en este proyecto Android.

## 📋 Configuración Actual

### JaCoCo (Cobertura de Código)

JaCoCo está configurado para generar reportes de cobertura de código de las pruebas unitarias.

#### Ubicación de la configuración:
- **Archivo principal**: `app/build.gradle`
- **Plugin aplicado**: `jacoco`
- **Versión**: 0.8.11

#### Configuración en `app/build.gradle`:

```gradle
apply plugin: 'jacoco'

jacoco {
    toolVersion = "0.8.11"
}

android {
    buildTypes {
        debug {
            testCoverageEnabled true
        }
    }
}
```

#### Task personalizada `jacocoTestReport`:

```gradle
task jacocoTestReport(type: JacocoReport, dependsOn: 'testDebugUnitTest') {
    reports {
        xml.required = true
        html.required = true
        xml.outputLocation = file("${buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        html.outputLocation = file("${buildDir}/reports/jacoco/jacocoTestReport/html")
    }
    
    // Exclusiones (archivos que no se incluyen en la cobertura)
    def excludeFilter = [
        '**/R.class',
        '**/R$*.class',
        '**/BuildConfig.*',
        '**/Manifest*.*',
        '**/*Test*.*',
        'android/**/*.*',
        '**/databinding/**/*.*',
        '**/android/databinding/*Binding.*'
    ]
    
    // Configuración de directorios
    classDirectories.setFrom(...)
    sourceDirectories.setFrom(...)
    executionData.setFrom(...)
}
```

### SonarQube (Análisis de Calidad)

SonarQube está configurado para analizar la calidad del código y usar los reportes de JaCoCo.

#### Ubicación de la configuración:
- **Archivo raíz**: `build.gradle` (proyecto raíz)
- **Archivo app**: `app/build.gradle`
- **Plugin aplicado**: `org.sonarqube`
- **Versión**: 6.2.0.5505

#### Configuración en el `build.gradle` raíz:

```gradle
plugins {
    id "org.sonarqube" version "6.2.0.5505"
}

sonar {
    properties {
        property "sonar.projectKey", "aimarsanm_LKS_Parking_Aimar"
        property "sonar.organization", "aimarsanm-1"
        property "sonar.host.url", "https://sonarcloud.io"
        property "sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
    }
}
```

#### Configuración adicional en `app/build.gradle`:

```gradle
sonar {
    properties {
        property "sonar.projectKey", "aimarsanm_LKS_Parking_Aimar"
        property "sonar.organization", "aimarsanm-1"
        property "sonar.host.url", "https://sonarcloud.io"
        property "sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
        property "sonar.exclusions", "**/R.class,**/R$*.class,**/BuildConfig.class,**/*Test*.class,**/databinding/**/*.*,**/android/databinding/*Binding.*"
        property "sonar.coverage.exclusions", "**/R.class,**/R$*.class,**/BuildConfig.class,**/*Test*.class,**/databinding/**/*.*,**/android/databinding/*Binding.*"
        property "sonar.test.inclusions", "**/*Test*.java,**/*Test*.kt"
        property "sonar.sources", "src/main/java"
        property "sonar.tests", "src/test/java"
    }
}
```

## 🚀 GitHub Actions

El workflow de GitHub Actions está configurado en `.github/workflows/build.yml`:

```yaml
name: SonarQube Analysis

on:
  push:
    branches: [ main ]
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  sonarqube:
    name: Build and Analyze with SonarQube
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: 17
          distribution: 'zulu'

      - name: Cache Gradle packages
        uses: actions/cache@v4
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle') }}

      - name: Grant execute permission for gradlew
        run: chmod +x ./gradlew

      - name: Run Unit Tests and Generate Coverage Report
        run: ./gradlew test jacocoTestReport

      - name: Run SonarQube Analysis
        run: ./gradlew sonar --info
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

## 🔧 Comandos para usar localmente

### Generar reporte de cobertura:
```bash
./gradlew test jacocoTestReport
```

### Generar reporte limpio:
```bash
./gradlew coverageReport
```

### Analizar con SonarQube (requiere SONAR_TOKEN):
```bash
./gradlew sonar
```

### Ver reportes generados:
- **JaCoCo HTML**: `app/build/reports/jacoco/jacocoTestReport/html/index.html`
- **JaCoCo XML**: `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`

## 📁 Estructura de archivos generados

```
app/build/
├── reports/
│   └── jacoco/
│       └── jacocoTestReport/
│           ├── html/
│           │   └── index.html (reporte visual)
│           └── jacocoTestReport.xml (para SonarQube)
└── jacoco/
    └── testDebugUnitTest.exec (datos de ejecución)
```

## ⚙️ Variables de entorno necesarias

Para que SonarQube funcione en GitHub Actions, necesitas configurar en los **Secrets** del repositorio:

- `SONAR_TOKEN`: Token de autenticación de SonarCloud
- `GITHUB_TOKEN`: Token automático de GitHub (ya disponible)

## 🎯 Archivos excluidos del análisis

Los siguientes archivos se excluyen automáticamente del análisis de cobertura:

- Clases R generadas por Android
- BuildConfig
- Archivos de test
- Clases de DataBinding
- Archivos de Manifest

## 📊 Resultados

Una vez configurado y ejecutado:

1. **JaCoCo** genera reportes de cobertura de código mostrando qué líneas están cubiertas por las pruebas
2. **SonarQube** analiza la calidad del código y usa los datos de JaCoCo para mostrar la cobertura
3. Los resultados aparecen en **SonarCloud** en: https://sonarcloud.io/organizations/aimarsanm-1/projects

## 🔗 Enlaces útiles

- [Documentación JaCoCo](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
- [Documentación SonarQube Gradle](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-gradle/)
- [SonarCloud](https://sonarcloud.io/)