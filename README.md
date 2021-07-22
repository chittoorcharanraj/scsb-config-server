## Phase4-SCSB-Config-Server

   SCSB Cloud Config provides server-side and client-side support for externalized configuration in a distributed system. With the Config Server, you have a central place to manage external properties for applications across all environments.
### Prerequisite
          - Java 11
          - Docker 19.03.13      
          
### Build
   Download the Project , navigate inside project folder and  build the project using below command
   * ./gradlew clean build -x test *

### Docker Image Creation
   Naviagte Inside project folder where Dockerfile is present and Execute the below command
* sudo docker build -t phase4-scsb-config-server  . *

