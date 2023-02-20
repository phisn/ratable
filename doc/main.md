# Introduction
- There are crdt and local first application
- Why we need authorization in modern applications
- CRDTs do not allow authentication
- Propose ECmRDT to enable authentication and authorization
- Additionally ECmRDT will enable us to create a variaty of extensions
- Working on a case study including ECmRDT
- case study will need a new type of architecture
- introduce concepts how local first applications can be build
- Enabled us to implement new use cases not possible previously

# Case Study
- What ratable is
- Special features of ratable compared to normal applications
- How ECmRDT enables us to implement these features

# ECmRDT
- Very brief overview of what ECmRDTs are
- What ECmRDT tries to accomplish
- How ECmRDT work and how do they accomplish their goal
- Make an example and explain how this is useful and how it solves this example
  (trying) to make it more concrete
## Concepts
- Go into detail and introduce all concepts
- Explain how concepts relate to each other
- Explain how extensions work
## Authentication and authorization
- How authentication and authorization extensions can be developed
## Integration into ratable
- How the ratable domain is designed using ECmRDT
- Specific benifits of ECmRDT in Ratable

# Architecture
- Explain three projects composing ratable
- Why we decided against peer to peer
- Additional services used shown in a diagram to get an overall view
## Project Structure
- Project consists of layers
- Device layer for hardware abstraction
- Application layer containing use cases and additionally ui
- Structure enabling testing of each use case by mocking device layer only
## State managment
- What our state managment can do
- How users interact with our state
- Inner and outer aggregate abstractions
- How state is managed between abstractions and outer access
- How side effects for distribution and co work

# Future work
- Real peer to peer systems using ecmrdt's






# Introduction (should explain solution)
- Propose a custom type of CRDT to solve authentication problems
- Local first
- Implement a case study with focus on local first / cloud
- Why do we need a custom architecture

Motivation:
- Explain why we would need authentication authorization
- Explain why CRDTs are not be enough

# Case study

# ECmRDT
- Explain that we will look into and propose a CRDT to allow authentication
## Overview
- Explain what ECmRDTs are and how they solve the problem of CRDTs
- Explain technically how a ECmRDT would solve the problem
- Maybe show a code example how the previous example would look like
## Concepts
- Tell that we will look into how we implement a simple CmRDT
- Tell how we improve the CmRDT to our ECmRDT
### Fundamental
- Explain all core concepts of ECmRDT and how they relate to each other
### Extensions
- Tell why extensions are needed
- Explain how extensions work
- Explain what type of extensions can be build
## Authorization and Authentication
- Explain how an extension can be designed to enable authentication
- Explain other ways of achieving authentication (non cryptographic)
- Explain why an extension implemented like here is needed (cryptographic)
## Conclusion
- Explain that we will use this approach in ratable 
  and show it can be used in practice
- Explain that we will look into possibilities in future work

# Architecture

# Implementation Ratable
- How to integrate ECmRDT into reallife
- How to architect scala project
- How to implement cloud application

## Case study
- Explain what Ratable is
- Use cases of Ratable
- What the focus of Ratable will be

## Architecture
- Explain overall architecture
- Explain choice of technologies
- Explain core / webapp / functions differentation

### Services
- Explain used services
- Show used services and interactions with graph

### Development Cycle
- Explain and show development cycle in a graph (cicd and terraform)

## Webapp
- Explain what the webapp is and what it does
### Application layer
- What is the layer responsible for
- Use case oriented approach and why
- Explain how the application layer is split

### State layer
- What is the layer responsible for
- How the state is structured and accessible
- Explain internal structering
- How distribution is handled
- How saving is handled

### Device layer
- General responsibility: Abstracting all webbrowser hardware
- Abstracting indexdb for scala
- Abstracting subtle Crypto library
- Abstracting connectivity by websockets and http

## Core
- General responsibility: Domain logic, protobuf (messages) and libraries like ECmRDT
- Go into detail of ratable domain logic (especially to showcase usage of ECmRDT)
- 

# Future Work
- Explain how ECmRDT can be used in peer to peer

<!--
/# Background and motivation
/- Explain that we will look into core concepts
/## CvRDT and CmRDT
/- Explain what CRDTs are
- Explain difference between CvRDT and CmRDT
-->