FROM blazemeter/taurus
WORKDIR /app
COPY . .
ENTRYPOINT ["bzt"]
CMD ["taurus.yaml"]
