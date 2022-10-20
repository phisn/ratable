package backend.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Source: https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/webpubsub/Microsoft.Azure.WebJobs.Extensions.WebPubSub/src/Trigger/WebPubSubTriggerAttribute.cs
// Test: https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/webpubsub/Microsoft.Azure.WebJobs.Extensions.WebPubSub/samples/WebPubSubTriggerFunction.cs
// Bundle Extensions: https://github.com/Azure/azure-functions-extension-bundles/blob/v3.x-preview/src/Microsoft.Azure.Functions.ExtensionBundle/extensions.json
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface WebPubSubTrigger {
  String hub();
  String EventName();
  WebPubSubEventType EventType();
  String[] connections();
}
