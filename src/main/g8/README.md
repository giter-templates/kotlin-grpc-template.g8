# $name$

## build

```shell
gradle 
client/dockerBuildImage
server/dockerBuildImage
```

## run

```shell
docker-compose up
```

- localhost:8081 -- server metrics
- localhost:8082 -- client metrics
- localhost:16686 -- traces