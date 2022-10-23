terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0.0"
    }
  }
  required_version = ">= 0.14.9"
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = "rg-localrating"
  location = "westeurope"
}

// https://docs.microsoft.com/en-us/azure/static-web-apps/overview
// https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/static_site
resource "azurerm_static_site" "staticsite" {
  name                = "stapp-localrating"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
}

resource "azurerm_web_pubsub" "web_pubsub" {
  name                = "wps-localrating"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name

  sku = "Free_F1"
}

// linux functions app does currently not support zip deploy. 
// needed for github actions cicd.
// https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/linux_function_app
resource "azurerm_storage_account" "storage" {
  name                     = "stlocalrating"
  resource_group_name      = azurerm_resource_group.rg.name
  location                 = azurerm_resource_group.rg.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
}

resource "azurerm_service_plan" "backendplan" {
  name                = "plan-localrating-backend"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  os_type             = "Windows"
  sku_name            = "Y1"
}

resource "azurerm_windows_function_app" "backend" {
  name                = "func-localrating-backend"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location

  storage_account_name       = azurerm_storage_account.storage.name
  storage_account_access_key = azurerm_storage_account.storage.primary_access_key
  service_plan_id            = azurerm_service_plan.backendplan.id

  site_config {
    cors {
      allowed_origins = ["*"]
    }
  }
}

output "api_key" {
  value = azurerm_static_site.staticsite.api_key
}

output "hostname" {
  value = azurerm_static_site.staticsite.default_host_name
}

output "api_hostname" {
  value = azurerm_windows_function_app.backend.default_hostname
}

output "webpubsub_connection_string" {
  sensitive = true
  value = azurerm_web_pubsub.web_pubsub.primary_connection_string
}
