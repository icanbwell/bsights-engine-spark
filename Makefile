build:
# 	docker image rm imranq2/cql-evaluator-local || echo "no image"
	docker build -t imranq2/cql-spark .

shell:
	docker run -it imranq2/cql-spark sh

up:
	docker build -t imranq2/cql-spark . && \
	docker run --name cql-spark --rm imranq2/cql-spark

Pipfile.lock: Pipfile
	docker-compose run --rm --name helix_pipenv dev rm -f Pipfile.lock && pipenv lock --dev --clear

.PHONY:update
update:  ## Updates all the packages using Pipfile # (it takes a long time) make run-pre-commit
	docker-compose run --rm --name helix_pipenv dev pipenv sync
