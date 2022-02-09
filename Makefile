build:
# 	docker image rm imranq2/cql-evaluator-local || echo "no image"
	docker build -t imranq2/cql-spark .

shell:
	docker build -t imranq2/cql-spark . && \
    docker run -it imranq2/cql-spark sh

up1:
	docker build -t imranq2/cql-spark . && \
	docker run --name cql-spark --rm imranq2/cql-spark

.PHONY:up
up: ## Brings up all the services in docker-compose
	echo "`docker-compose --version | awk '{print $$4;}'`" && \
	if [[ "`docker-compose --version | awk '{print $$4;}'`" == "v2."* ]]; then echo "ERROR: Docker Compose version should be < 2.  Uncheck Use Docker Compose V2 checkbox in Docker Settings and restart Docker." && exit 1; fi && \
	docker-compose -f docker-compose.yml up --build --no-start && \
	docker-compose -f docker-compose.yml up -d --force-recreate  && \
	echo "\nwaiting for Mongo server to become healthy" && \
	while [ "`docker inspect --format {{.State.Health.Status}} bsights_engine_spark_mongodb`" != "healthy" ] && [ "`docker inspect --format {{.State.Health.Status}} bsights_engine_spark_mongodb`" != "unhealthy" ] && [ "`docker inspect --format {{.State.Status}} bsights_engine_spark_mongodb`" != "restarting" ]; do printf "." && sleep 2; done && \
	if [ "`docker inspect --format {{.State.Health.Status}} bsights_engine_spark_mongodb`" != "healthy" ]; then docker ps && printf "========== ERROR: bsights_engine_spark_mongodb did not start. Run docker logs bsights_engine_spark_mongodb =========\n" && exit 1; fi && \
	echo "\nwaiting for Fhir server to become healthy" && \
	while [ "`docker inspect --format {{.State.Health.Status}} bsights_engine_spark_fhir`" != "healthy" ] && [ "`docker inspect --format {{.State.Health.Status}} bsights_engine_spark_fhir`" != "unhealthy" ] && [ "`docker inspect --format {{.State.Status}} bsights_engine_spark_fhir`" != "restarting" ]; do printf "." && sleep 2; done && \
	if [ "`docker inspect --format {{.State.Health.Status}} bsights_engine_spark_fhir`" != "healthy" ]; then docker ps && printf "========== ERROR: bsights_engine_spark_fhir did not start. Run docker logs bsights_engine_spark_fhir =========\n" && exit 1; fi


.PHONY:down
down: ## Brings down all the services in docker-compose
	export DOCKER_CLIENT_TIMEOUT=240 && export COMPOSE_HTTP_TIMEOUT=240 && \
	docker-compose down --remove-orphans && \
	docker system prune -f

.PHONY:buildjar
buildjar:  ## Updates all the packages using Pipfile # (it takes a long time) make run-pre-commit
	mvn clean && mvn -Dmaven.test.skip package && \
	mvn dependency:copy-dependencies -DoutputDirectory=target/jars -Dhttps.protocols=TLSv1.2

.PHONY:loadfhir
loadfhir:
	docker-compose run scriptrunner bash -c "pip install requests && ls / && cd /scripts && python3 load_fhir_server.py"

.PHONY: clean_data
clean_data: down ## Cleans all the local docker setup
ifneq ($(shell docker volume ls | grep "bsights_engine_spark"| awk '{print $$2}'),)
	docker volume ls | grep "bsights_engine_spark" | awk '{print $$2}' | xargs docker volume rm
endif

.PHONY: tests
tests: loadfhir
	mvn test
