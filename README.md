<p align="center">
    <img src="https://raw.githubusercontent.com/otaviof/ravine/master/assets/logo/ravine.png"/>
</p>
<p align="center">
    <a alt="Code Coverage" href="https://codecov.io/gh/otaviof/ravine">
        <img alt="Code Coverage" src="https://codecov.io/gh/otaviof/ravine/branch/master/graph/badge.svg">
    </a>
    <a alt="CI Status" href="https://travis-ci.com/otaviof/ravine">
        <img alt="CI Status" src="https://travis-ci.com/otaviof/ravine.svg?branch=master">
    </a>
    <a alt="Docker-Cloud Build Status" href="https://hub.docker.com/r/otaviof/ravine">
        <img alt="Docker-Cloud Build Status" src="https://img.shields.io/docker/cloud/build/otaviof/ravine.svg">
    </a>
</p>

# `ravine`

Ravine works as a bridge between HTTP requests and Kafka messages. It handles HTTP requests using Confluent
Schema-Registry to validate contracts, and then dispatch a message in Kafka, waiting to receive a response
message from another topic.

Therefore, it enables you to focus on Kafka based applications, while Ravine will handle external world
HTTP requests. 

<p align="center">
    <img alt="Basic workflow" src="https://raw.githubusercontent.com/otaviof/ravine/master/assets/diagrams/ravine-diag.png"/>
</p>
