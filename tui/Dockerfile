FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1
RUN apt-get update && apt-get install -y sbt libxrender1 libxtst6 libxi6
WORKDIR /app

ADD . /app

EXPOSE 9005

CMD sbt run