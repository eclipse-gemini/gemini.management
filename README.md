# Eclipse Gemini Management

TL;DR

```shell
docker run --rm -v "$(pwd)":/usr/src/app --workdir=/usr/src/app maven:3.9.4-eclipse-temurin-8 mvn verify 
```

## Build with Docker

> Tip - You can speed up local builds using an `.m2` cache
>```shell
>docker volume create --name maven-repo
>```

The kick off the build with:

```shell
docker run -it --rm -v maven-repo:/root/.m2 -v "$(pwd)":/usr/src/app --workdir=/usr/src/app --entrypoint=/bin/bash maven:3.9.4-eclipse-temurin-8
```
