#!/bin/bash

# Script para verificar la configuración de JaCoCo y SonarQube
# Usage: ./verify_jacoco_setup.sh

echo "🔍 Verificando configuración de JaCoCo y SonarQube..."

# Verificar archivos de configuración
echo "📁 Verificando archivos de configuración..."

if [ -f "app/build.gradle" ]; then
    echo "✅ app/build.gradle existe"
    
    if grep -q "apply plugin: 'jacoco'" app/build.gradle; then
        echo "✅ Plugin JaCoCo está aplicado"
    else
        echo "❌ Plugin JaCoCo no está aplicado"
    fi
    
    if grep -q "task jacocoTestReport" app/build.gradle; then
        echo "✅ Task jacocoTestReport está configurado"
    else
        echo "❌ Task jacocoTestReport no está configurado"
    fi
    
    if grep -q "testCoverageEnabled true" app/build.gradle; then
        echo "✅ testCoverageEnabled está habilitado"
    else
        echo "❌ testCoverageEnabled no está habilitado"
    fi
else
    echo "❌ app/build.gradle no existe"
fi

if [ -f "build.gradle" ]; then
    echo "✅ build.gradle (raíz) existe"
    
    if grep -q "org.sonarqube" build.gradle; then
        echo "✅ Plugin SonarQube está aplicado"
    else
        echo "❌ Plugin SonarQube no está aplicado"
    fi
else
    echo "❌ build.gradle (raíz) no existe"
fi

if [ -f ".github/workflows/build.yml" ]; then
    echo "✅ GitHub Actions workflow existe"
    
    if grep -q "jacocoTestReport" .github/workflows/build.yml; then
        echo "✅ JaCoCo está configurado en GitHub Actions"
    else
        echo "❌ JaCoCo no está configurado en GitHub Actions"
    fi
    
    if grep -q "sonar" .github/workflows/build.yml; then
        echo "✅ SonarQube está configurado en GitHub Actions"
    else
        echo "❌ SonarQube no está configurado en GitHub Actions"
    fi
else
    echo "❌ GitHub Actions workflow no existe"
fi

# Verificar estructura de tests
echo "🧪 Verificando estructura de tests..."

if [ -d "app/src/test/java" ]; then
    echo "✅ Directorio de tests existe"
    
    test_count=$(find app/src/test/java -name "*Test.java" | wc -l)
    echo "📊 Tests encontrados: $test_count"
    
    if [ $test_count -gt 0 ]; then
        echo "✅ Se encontraron tests"
        find app/src/test/java -name "*Test.java" | head -5 | while read file; do
            echo "   - $(basename "$file")"
        done
    else
        echo "⚠️  No se encontraron tests"
    fi
else
    echo "❌ Directorio de tests no existe"
fi

echo ""
echo "📝 Resumen de la configuración:"
echo "   - JaCoCo: Configurado para generar reportes de cobertura"
echo "   - SonarQube: Configurado para análisis de calidad"
echo "   - GitHub Actions: Configurado para CI/CD automático"
echo ""
echo "🚀 Para ejecutar localmente:"
echo "   ./gradlew test jacocoTestReport"
echo "   ./gradlew coverageReport (task personalizado)"
echo ""
echo "📊 Los reportes se generan en:"
echo "   app/build/reports/jacoco/jacocoTestReport/html/index.html"
echo "   app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"