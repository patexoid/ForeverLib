VERSION = $(shell git describe --tags --dirty)

build:
	docker build  --build-arg USERNAME=$(USERNAME) --build-arg  TOKEN=$(TOKEN) -t patexoid/zombielib2:$(VERSION) .


push:
	echo "$(HUB)" | docker login --username patexoid --password-stdin
	docker push patexoid/zombielib2:$(VERSION)
