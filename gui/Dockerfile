FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

ENV DISPLAY=host.docker.internal:0.0

RUN apt-get update && \
    apt-get install -y --no-install-recommends openjfx && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get install -y sbt libxrender1 libxtst6 libxi6

WORKDIR /app

ADD . /app

EXPOSE 9004

CMD sbt run