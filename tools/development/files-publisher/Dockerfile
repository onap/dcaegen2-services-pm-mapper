FROM nexus3.onap.org:10001/alpine:latest

RUN apk add --update curl
RUN rm -rf /var/cache/apk/*
ENV http_proxy=""
ENV https_proxy=""
ENV HTTP_PROXY=""
ENV HTTPS_PROXY=""
ENTRYPOINT ["/bin/sh", "-c", " while true; do sleep 30; done;"]
