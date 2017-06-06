pipeline {
    agent any
    tools {
        maven 'Maven339' 
    }

    stages {
        stage('Compile') {
            steps {
                git branch: 'pipeline-tests-integration', credentialsId: '83c58be8-3bb5-49a7-9bf2-947b898785ab', url: 'git@github.ibm.com:CASE/refarch-jee-customerorder'
                sh "cd CustomerOrderServicesProject ; mvn clean install"
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
    }
}
