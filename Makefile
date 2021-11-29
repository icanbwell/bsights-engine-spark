build:
# 	docker image rm imranq2/cql-evaluator-local || echo "no image"
	docker build -t imranq2/cql-spark .

shell:
	docker run -it imranq2/cql-spark sh

up1:
	docker build -t imranq2/cql-spark . && \
	docker run --name cql-spark --rm imranq2/cql-spark

.PHONY:up
up: ## Brings up all the services in docker-compose
	echo "`docker-compose --version | awk '{print $$4;}'`" && \
	if [[ "`docker-compose --version | awk '{print $$4;}'`" == "v2."* ]]; then echo "ERROR: Docker Compose version should be < 2.  Uncheck Use Docker Compose V2 checkbox in Docker Settings and restart Docker." && exit 1; fi && \
	docker-compose -f docker-compose.yml up --build --no-start --no-recreate && \
	docker-compose -f docker-compose.yml up -d --no-recreate

.PHONY:down
down: ## Brings down all the services in docker-compose
	export DOCKER_CLIENT_TIMEOUT=240 && export COMPOSE_HTTP_TIMEOUT=240 && \
	docker-compose down --remove-orphans && \
	docker system prune -f

.PHONY:buildjar
buildjar:  ## Updates all the packages using Pipfile # (it takes a long time) make run-pre-commit
	mvn clean && mvn package && \
	mvn dependency:copy-dependencies -DoutputDirectory=target/jars -Dhttps.protocols=TLSv1.2

.PHONY:loadfhir
loadfhir: up
	docker-compose run scriptrunner bash -c "pip install requests && ls / && cd /scripts && python3 load_fhir_server.py"

.PHONY: clean_data
clean_data: down ## Cleans all the local docker setup
ifneq ($(shell docker volume ls | grep "helixbsightscql_spark_engine"| awk '{print $$2}'),)
	docker volume ls | grep "helixbsightscql_spark_engine" | awk '{print $$2}' | xargs docker volume rm
endif

.PHONY: tests
tests:
	mvn test
