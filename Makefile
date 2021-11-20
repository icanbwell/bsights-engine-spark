build:
# 	docker image rm imranq2/cql-evaluator-local || echo "no image"
	docker build -t imranq2/cql-spark .

shell:
	docker run -it imranq2/cql-spark sh

up:
	docker build -t imranq2/cql-spark . && \
	docker run --name cql-spark --rm imranq2/cql-spark
