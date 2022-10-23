/**
 * Handling deltas
 * 
 * 1. Get from sender
 * 2. Apply internally
 * 3. Distribute to others
 * 4. Acknowledge sender
 */
module.exports = async function (context, data) {
  context.bindings.actions = {
    "actionName": "sendToAll",
    "data": data,
    "dataType": "json"
  }

  context.done()

  /*
  return { 
    "data": JSON.stringify({
    }),
    "dataType": "json"
  }
  */
}