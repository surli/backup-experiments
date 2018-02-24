#!/usr/bin/env bash
#本地部署 现在也可以支持 本地直接打包image然后推送
# 第一步先login到 docker; docker login --username xx@mingshz registry.cn-shanghai.aliyuncs.com
# 然后调用本shell, 后面随便跟一个餐宿那就是推送
mkdir logs
mvn -Dmaven.test.skip=true clean install
cd web
if [ -z "$1" ]; then
mvn -Dmaven.test.skip=true io.fabric8:docker-maven-plugin:0.23.0:build
cd ..
docker stack deploy --compose-file local-docker-compose.yml sb
else
mvn -Dmaven.test.skip=true io.fabric8:docker-maven-plugin:0.23.0:build io.fabric8:docker-maven-plugin:0.23.0:push
fi
#mvn -Dmaven.test.skip=true io.fabric8:docker-maven-plugin:0.23.0:build
#cd ..
#docker stack deploy --compose-file local-docker-compose.yml sb

#使用以下指令 删除集群
#docker stack rm sb

#若有必要删除mysql 数据就
#docker volume rm sb_database