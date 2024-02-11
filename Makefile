VERSION = $(shell git describe --tags --dirty)

build:
	docker build  --build-arg USERNAME=$(USERNAME) --build-arg  TOKEN=$(TOKEN) -t patexoid/foreverlib:$(VERSION) .


push:
	docker push patexoid/foreverlib:$(VERSION)
