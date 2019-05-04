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

Ravine works as a bridge between synchronous HTTP based workload, and *event-driven* applications 
landscape. It acts as a bridge holding HTTP requests while it fires a request message in [Kafka][kafka] and 
wait for a response, therefore other applications can rely in this service to offload HTTP communication,
while Ravine rely in [Confluent Schema-Registry][schemaregistry] to maintain API contracts.

The project goal is empower developers to focus on event-driven eco-systems, and easily plug HTTP endpoints
against existing Kafka based landscapes.

<p align="center">
    <a alt="Basic Ravie workflow" href="https://draw.io/?title=ravine-diag.png&url=https%3A%2F%2Fraw.githubusercontent.com%2Fotaviof%2Fravine%2Fspam%2Fassets%2Fdiagrams%2Fravine-diag.png">
        <img alt="Basic workflow" src="https://raw.githubusercontent.com/otaviof/ravine/master/assets/diagrams/ravine-diag.png"/>
    </a>
</p>

## Usage

Ravine is based on defining HTTP endpoints and mapping request and response topics. On request topic, it
will send payload received during HTTP communication, the payload is serialized with configured 
[Schema-Registry Subject][schemaregistrysubject] version, and dispatched on request topic. Ravine will wait
for configured timeout, to receive a message in output topic, having the same unique key, where the 
message value is used as response payload for ongoing HTTP request.

The following diagram represents the high level relationship between Ravine and Kafka topics.

<p align="center">
    <a alt="Ravine using Kafka topics workflow" href="https://draw.io/?title=ravine-topics-diag.png&url=https%3A%2F%2Fraw.githubusercontent.com%2Fotaviof%2Fravine%2Fspam%2Fassets%2Fdiagrams%2Fravine-topics-diag.png">
        <img alt="Ravine using Kafka topics workflow" src="https://raw.githubusercontent.com/otaviof/ravine/master/assets/diagrams/ravine-topics-diag.png"/>
    </a>
</p>

## Development

In order to run tests locally, you need to spin up the necessary backend systems, namely
[Apache Kafka][kafka] (which depends on [Apache ZooKeeper][zookeeper]), and
[Confluent Schema-Registry][scehmaregistry]. They are wired up using [Docker-Compose](./docker-compose.yaml)
in a way that Ravine can reach requested backend, and using `localtest.me`[localtestme] hostnames to 
simulate DNS for Kafka.

The steps required on your laptop are the same needed during CI, so please consider
[`travis.yml`](./.travis.yml) as documentation.

[kafka]: https://kafka.apache.org
[zookeeper]: https://zookeeper.apache.org
[schemaregistry]: https://www.confluent.io/confluent-schema-registry
[schemaregistrysubject]: https://docs.confluent.io/current/schema-registry/index.html#schemas-subjects-and-topics
[localtestme]: http://readme.localtest.me
