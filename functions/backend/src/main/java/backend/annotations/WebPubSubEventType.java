package backend.annotations;

// Source: https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/webpubsub/Microsoft.Azure.WebPubSub.Common/src/WebPubSubEventType.cs
public enum WebPubSubEventType
{
  /// <summary>
  /// system event, including connect, connected, disconnected.
  /// </summary>
  System,
  /// <summary>
  /// user event.
  /// </summary>
  User
}
