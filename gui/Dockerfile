FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

RUN apt-get update && \
    apt-get install -y --no-install-recommends openjfx && \
    apt-get install -y \
    libxext6 \
    libxrender1 \
    libxtst6

WORKDIR /app

ADD . /app

EXPOSE 9004

CMD sbt run