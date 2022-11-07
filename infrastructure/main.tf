terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.30.0"
    }
  }
  required_version = ">= 0.14.9"
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "core" {
  name     = "rg-ratable"
  location = "westeurope"
}

// https://docs.microsoft.com/en-us/azure/static-web-apps/overview
// https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/static_site
resource "azurerm_static_site" "core" {
  name                = "stapp-ratable"
  location            = azurerm_resource_group.core.location
  resource_group_name = azurerm_resource_group.core.name
}

// linux functions app does currently not support zip deploy. 
// needed for github actions cicd.
// https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/linux_function_app
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

// Web Pubsub
// Needed to get webpubsub token
data "azurerm_function_app_host_keys" "core" {
  name                = azurerm_windows_function_app.core.name
  resource_group_name = azurerm_resource_group.core.name
}

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

// Outputs
output "api_key" {
  value = azurerm_static_site.core.api_key
}

output "hostname" {
  value = azurerm_static_site.core.default_host_name
}

output "api_hostname" {
  value = azurerm_windows_function_app.core.default_hostname
}

output "webpubsub_test_connection_string" {
  sensitive = true
  value = azurerm_web_pubsub.test.primary_connection_string
}
