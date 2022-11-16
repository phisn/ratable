// Needed to get webpubsub token
resource "azurerm_web_pubsub" "core" {
  name                = "wps-ratable"
  location            = azurerm_resource_group.core.location
  resource_group_name = azurerm_resource_group.core.name

  sku = "Free_F1"
}

resource "azurerm_web_pubsub_hub" "core_socket" {
  name          = "socket"
  web_pubsub_id = azurerm_web_pubsub.core.id

  event_handler {
    url_template       = format(
      "https://%s/runtime/webhooks/webpubsub?code=%s", 
      azurerm_windows_function_app.core.default_hostname, 
      data.azurerm_function_app_host_keys.core.webpubsub_extension_key)
    user_event_pattern = "*"
    system_events      = ["connect", "connected", "disconnected"]
  }
  
  depends_on = [
    azurerm_web_pubsub.core
  ]
}

resource "azurerm_web_pubsub" "test" {
  name                = "wps-ratable-test"
  location            = azurerm_resource_group.core.location
  resource_group_name = azurerm_resource_group.core.name

  sku = "Free_F1"
}
