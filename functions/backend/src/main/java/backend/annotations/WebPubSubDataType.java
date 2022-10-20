package backend.annotations;

// Source: https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/webpubsub/Microsoft.Azure.WebPubSub.Common/src/WebPubSubDataType.cs
/// <summary>
/// Message data type.
/// </summary>
public enum WebPubSubDataType
{
  /// <summary>
  /// binary of content type application/octet-stream.
  /// </summary>
  Binary,
  /// <summary>
  /// json of content type application/json.
  /// </summary>
  Json,
  /// <summary>
  /// text of content type text/plain.
  /// </summary>
  Text
}
