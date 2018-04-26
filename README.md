# kraft
Streaming Raft algorithm implementation in kotlin

## Background
**[Raft](https://raft.github.io/)** is a consensus algorithm created by 
[Diego Ongaro](https://twitter.com/ongardie) in an attempt to replace 
Paxos with a simpler and more understandable consensus algorithm.

The original [raft paper can be found here](https://raft.github.io/raft.pdf).

## Project
The idea behind this project, is to create a streaming raft implementation
written in kotlin, by using the concept of a sliding unacked data window.
This means that the leader will NOT wait for every follower ACK before sending
more data, it will just keep pushing data until the window is full or the ACK
is received releasing space from the in-flight data window.

This is a personal *pet project*, so it is still lacking automated tests, comments and
overall documentation. This might be fixed in the future, if I have time.

## Motivation
Back in 2015, while working at [PaddyPower Betfair](https://www.paddypowerbetfair.com/)
there was a need to create a streaming platform that could replace kafka and be resilient to network partitions,
as it was proven that [kafka could not handle network partitions very well](https://aphyr.com/posts/293-jepsen-kafka).

After some due dilligence, the team decided to write their own in-house implementation of the Raft algorithm,
with a few slight modifications to allow increased performance without sacrificing the consistency.
