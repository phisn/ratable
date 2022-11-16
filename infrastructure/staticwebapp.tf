// https://docs.microsoft.com/en-us/azure/static-web-apps/overview
// https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/static_site
resource "azurerm_static_site" "core" {
  name                = "stapp-ratable"
  location            = azurerm_resource_group.core.location
  resource_group_name = azurerm_resource_group.core.name
}
