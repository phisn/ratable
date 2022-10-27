# StateDistribution
## Background
All clients are connected with an websocket to an azure pubsub. The clients can send deltas to the pubsub which then will be handled by a azure function. The question is how to verify that all deltas created by an client are received by the azure function without too much bandwidth overhead (ex. not sending the whole state after every change).

## Tagged
Each action done by a client creates a delta with an associated tag and stores it in a deltas list. The tag is a number selected by the highest current tag in the list plus one. The client then sends all deltas merged into one with one tag, the current highest tag in the list, to the azure function. The azure function now handles this merged delta and responds with the received tag. The client now removes all deltas with a tag equal to or below the response tag. Because the handling of deltas is idempotent, we do not have to look into the case of a failed response, the client can simply resend the merged delta without any issues. The question is if there can be any issues with data races. 

- Case one 'The client did not make any action': The client now can simply remove all existent deltas and be sure that the azure function handled all his known deltas. 

- Case two 'The client did make actions and sent successfully a new merged delta': The client now removes all deltas with a tag equal to or below the response tag. The server will get duplicate tags but this will not cause not any issues because the handling is idempotent.

- Case three 'The client did make actions and failed to send a new merged delta': The client now removes all deltas with a tag equal to or below the response tag and will resend the remaining deltas in future.
