ARG bztVersion=14.2
FROM blazemeter/taurus:${bztVersion}
WORKDIR /app
COPY . .
ENTRYPOINT ["bzt"]
CMD ["taurus.yaml"]
