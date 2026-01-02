pipeline {
    agent any
    
    // Aquí invocamos la herramienta que configuramos en el Paso 1
    tools {
        maven 'Maven-3' 
    }

    stages {
        stage('Bajar Código 📥') {
            steps {
                // Como usamos "Pipeline from SCM", el código ya se baja solo.
                echo 'El código ya está aquí gracias a Git...'
            }
        }
        
        stage('Construir y Testear 🔨') {
            steps {
                echo 'Compilando y ejecutando tests...'
                // Este es el comando mágico de Maven.
                // 'clean': limpia compilaciones viejas.
                // 'package': compila, pasa los tests y crea el archivo .jar
                sh 'mvn clean package' 
            }
        }
    }
    
    post {
        success {
            echo '¡Éxito! Guardando el archivo .jar...'
            // Spring Boot deja el ejecutable en la carpeta "target"
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
    }
}
