# pizza-demo

A prototype of the pizza ordering back end services running in an **Istio Service Mesh**.

## Prerequisites
### 1. [Apache Maven 3.8.3 or later](https://maven.apache.org/download.cgi)
### 2. JDK 11 or later
### 3. [minikube](https://minikube.sigs.k8s.io/docs/start/) running with at least 16384 MB of memory and 4 CPUs as recommended by [Istio platform setup](https://istio.io/latest/docs/setup/platform-setup/minikube/)
### 4. Istio installed and running
Follow https://istio.io/latest/docs/setup/getting-started/ closely:
1. Install Istio with demo profile
2. Label default namespace for Envoy sidecar proxies injection
3. Install Istio sample addons
4. Open Kiali dashboard
### 5. Ensure "minikube docker-env" is effective

## Building, deploying and running the services
The projects are fully integrated with JKube kubernetes-maven-plugin, so they can simply be deployed into k8s during maven build.
### 1. Clone the repository
```
git clone https://github.com/ivanlkc/pizza-demo.git
```
### 2. Delete previous deployment in kubernetes
```
mvn k8s:undeploy -Pkubernetes
```
### 3. Generate manifest to prepare for deployment
```
mvn clean k8s:resource -Pkubernetes
```
### 4. Compile, unit test, package, and send to minikube's Docker daemon
```
mvn package k8s:build -Pkubernetes
```
### 5. Deploy into Istio service mesh
```
mvn k8s:deploy -Pkubernetes -Dmaven.test.skip=true
```

## Troubleshooting
### 1. Inspect the H2 database using H2 Console
A NodePort is exposed at 30456 for the H2 database management web console.
Open a browser and visit
```
http://$(minikube ip):30456
```
Then put this into JDBC URL and Connect:
```
jdbc:h2:tcp://localhost:9092/mem:best_pizza_db;DB_CLOSE_DELAY=-1
```
To use a different port number than 30456, modify [the JKube deployment YML](pizza-database/src/main/jkube/pizza-database-h2-console-service.yml).
### 2. Inspect the pod status
Every service is configured with Spring Boot 2's **readinessProbe** and **livenessProbe**. Ensure the pods status are ready.
```
kubectl get po
```
### 3. Inspect the pod logs
```
kubectl describe po <pod name>
kubectl logs <pod name>
```
