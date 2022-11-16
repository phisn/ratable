resource "azurerm_storage_account" "core" {
  name                     = "stratable"
  resource_group_name      = azurerm_resource_group.core.name
  location                 = azurerm_resource_group.core.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
}

resource "azurerm_service_plan" "core" {
  name                = "plan-ratable-backend"
  resource_group_name = azurerm_resource_group.core.name
  location            = azurerm_resource_group.core.location
  os_type             = "Windows"
  sku_name            = "Y1"
}

// Linux functions app does currently not support zip deploy. So we must use Windows.
// Zip deploy is needed for github actions cicd.
// https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/linux_function_app
// https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/windows_function_app
resource "azurerm_windows_function_app" "core" {
  name                = "func-ratable-core"
  resource_group_name = azurerm_resource_group.core.name
  location            = azurerm_resource_group.core.location

  storage_account_name       = azurerm_storage_account.core.name
  storage_account_access_key = azurerm_storage_account.core.primary_access_key
  service_plan_id            = azurerm_service_plan.core.id

  app_settings = {
    WebPubSubConnectionString    = azurerm_web_pubsub.core.primary_connection_string
 //   CosmosDBConnectionString     = data.azurerm_cosmosdb_account.core.virtual_network_rule.primary_sql_connection_string
  }

  site_config {
    cors {
      allowed_origins = ["*"]
    }

    application_stack {
      node_version = "~16"
    }
  }
}

data "azurerm_function_app_host_keys" "core" {
  name                = azurerm_windows_function_app.core.name
  resource_group_name = azurerm_resource_group.core.name
}
