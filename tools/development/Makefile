setup-all: setup-local run-pm-mapper config-dmaap subscribe-pm-mapper

test:
	@echo test

setup-local:
	@./setup-local.sh

run-pm-mapper:
	@./run-pm-mapper.sh

config-dmaap:
	@./config-dmaap.sh

subscribe-pm-mapper:
	@./subscribe-pm-mapper.sh

clean-env:
	@./clean-environment.sh

list-files:
	@ls files-publisher/files
