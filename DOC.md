# StateDistribution
## Background
All clients are connected with an websocket to an azure pubsub. The clients can send deltas to the pubsub which then will be handled by a azure function. The question is how to verify that all deltas created by an client are received by the azure function without too much bandwidth overhead (ex. not sending the whole state after every change).

## Tagged
Each action done by a client creates a delta with an associated tag and stores it in a deltas list. The tag is a number selected by the highest current tag in the list plus one. The client then sends all deltas merged into one with one tag, the current highest tag in the list, to the azure function. The azure function now handles this merged delta and responds with the received tag. The client now removes all deltas with a tag equal to or below the response tag. Because the handling of deltas is idempotent, we do not have to look into the case of a failed response, the client can simply resend the merged delta without any issues. The question is if there can be any issues with data races. 

- Case one 'The client did not make any action': The client now can simply remove all existent deltas and be sure that the azure function handled all his known deltas. 

- Case two 'The client did make actions and sent successfully a new merged delta': The client now removes all deltas with a tag equal to or below the response tag. The server will get duplicate tags but this will not cause not any issues because the handling is idempotent.

- Case three 'The client did make actions and failed to send a new merged delta': The client now removes all deltas with a tag equal to or below the response tag and will resend the remaining deltas in future.

# Deployment
For easier deployment of the azure resources, we use a infrastructure as a code tool terraform. The creation and update of infrastructure itself has to be created manually. The deployment of the application is done by github actions. After every push to the master branch, the application is directly deployed to the azure.

# Azure functions
## Binding with scala
Because WebPubSubs are currently not supported in typescript nor java, we have to use the javascript azure functions. The scala backend is compiled into a single lib.js, that is accessed and called by each azure function.  

# Testing
The application has unit tests for core services and an test for each usecase. Because Scala 3 does not currently have an mocking framework, every service has a custom mock implementation. 

# Communication and persistence
All deltas are first converted to json for further use. For persisting on the client they are simply stored in localstorage. For communication with the azure function we have decided to use protobuf because often messages contain other messages. For example a server message would contain a delta message which would contain a json delta. This or even deeper nesting would result in a lot of overhead when using json. Protobuf is a binary format which is much more efficient and has native support for these types of oneof relationships (ex. server message contains oneof type of a message).

# Authentication, ReplicaTokens and ReplicaIDs
## Draft 1
Each ReplicaToken uniquely identifies a device. A client represents multiple ReplicaTokens. A ReplicaID is a hashed ReplicaToken. The seperation is required as a solution to the following problem. Clients may create aggregates and later sign in. Other replicas may already in this time aquire the temporary replicaID in an aggregate (ex. user creates a Ratable before sign in). After the sign in usally the ReplicaID would change to the original one created at first account creation. To solve this, a client has multiple ReplicaIDs. Each ReplicaID is a unique Device. Now a client should be able to aquire a ReplicaID, if it sign ins on a new device. To prevent random clients from aquiring a ReplicaID from other clients (that are not yet signed in), the process of aquiring it is to know a secret. The secret is the ReplicaToken, that gets hashed to the ReplicaID.

# Storage, Localstorage and IndexedDB
The project initialy used localstorage. Localstorage is limited to 5MB and does only provide synchronous access. The better solution was to use the more modern IndexedDB. It is asynchronous and has browser specific but usally very high limit. IndexedDB is supported on all modern browsers.

