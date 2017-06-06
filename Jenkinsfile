pipeline {
    agent any
    tools {
        maven 'Maven339' 
    }

    stages {
        stage('Compile') {
            steps {
                sh "cd CustomerOrderServicesProject ; mvn clean install -DskipTests"
            }
        
        }
        
        stage('Unit-Test') {
            steps {
                sh 'echo "When we grow up we will put some unit tests here...."'
            }
        }
        
        stage('Deploy') {
            steps {
                step([$class: 'UCDeployPublisher',
                    siteName: 'TaaS UCD', // Change to variable
                    component: [
                        $class: 'com.urbancode.jenkins.plugins.ucdeploy.VersionHelper$VersionBlock',
                        componentName: 'CustomerOrderServicesApp',
                        delivery: [
                            $class: 'com.urbancode.jenkins.plugins.ucdeploy.DeliveryHelper$Push',
                            pushVersion: 'pipeline-${BUILD_NUMBER}',
                            //baseDir: '${WORKSPACE}/CustomerOrderServicesApp/target',
                            baseDir: '/home/jenkins/workspace/test-pipeline/CustomerOrderServicesApp/target',
                            fileIncludePatterns: '**/*.ear',
                            fileExcludePatterns: '',
                            pushProperties: 'jenkins.server=Local\njenkins.reviewed=false',
                            pushDescription: 'Pushed from Jenkins',
                            pushIncremental: true
                        ]
                    ],
                    deploy: [
                        $class: 'com.urbancode.jenkins.plugins.ucdeploy.DeployHelper$DeployBlock',
                        deployApp: 'CustomerOrderServicesApp',
                        deployEnv: 'WASaaS-dev',
                        deployProc: 'PushApp',
                        deployVersions: 'CustomerOrderServicesApp:pipeline-${BUILD_NUMBER}',
                        deployOnlyChanged: false
                    ]
                ])
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'echo "Later we defenitely will run some integration tests here! I promise!"'
            }
        }
    }
}
