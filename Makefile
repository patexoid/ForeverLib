VERSION = $(shell git describe --tags --dirty)

build:
	docker build  --build-arg USERNAME=$(USERNAME) --build-arg  TOKEN=$(TOKEN) -t patexoid/zombielib2:$(VERSION) .


push:
	docker push patexoid/zombielib2:$(VERSION)
