FROM 2w1x0a76.gra7.container-registry.ovh.net/suinsit-proyect/jetty:10
ENV TZ=Europe/Madrid
EXPOSE 9092/tcp
ADD target/*.war /var/lib/jetty/webapps/root.war
