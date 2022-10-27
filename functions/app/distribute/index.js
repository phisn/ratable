/**
 * Handling deltas
 * 
 * 1. Get from sender
 * 2. Apply internally
 * 3. Distribute to others
 * 4. Acknowledge sender
 */
module.exports = async function (context, data) {
  context.log(`Connection ID: ${context.bindingData.connectionContext.connectionId}`);

  context.bindings.actions = {
    "actionName": "sendToAll",
    "data": data,
    "dataType": "json",
    "excluded": [ context.bindingData.connectionContext.connectionId ]
  }

  return {
    "data": JSON.stringify(
      
    ),
    "dataType": "json"
  }

  /*
  return { 
    "data": JSON.stringify({
    }),
    "dataType": "json"
  }
  */
}