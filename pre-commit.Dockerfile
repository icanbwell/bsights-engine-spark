FROM python:3.7-slim

RUN apt-get update && \
    apt-get install -y git && \
    pip install pre-commit

WORKDIR /sourcecode

CMD if [ "$PRE_COMMIT_ALL_FILES" = true ] ; then pre-commit run --all-files ; \
    else pre-commit run ; fi

